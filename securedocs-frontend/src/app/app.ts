import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';

type Section = 'documentos' | 'usuarios' | 'auditoria';
interface Session { token: string; tipo: string; usuarioId: number; nombre: string; correo: string; rol: string; }
interface Documento { id: number; titulo: string; descripcion?: string; propietario?: { id: number; nombre: string }; departamento?: { id: number; nombre: string }; nivelConfidencialidad: number; estado: string; pais: string; fechaCreacion: string; }
interface Usuario { id: number; nombre: string; correo: string; rol?: { id: number; nombre: string }; departamento?: { id: number; nombre: string }; nivelSeguridad: number; pais: string; tipoContrato: string; estado: string; }
interface Auditoria { id: number; usuario?: { nombre: string; correo: string }; recurso: string; accion: string; fecha: string; resultado: string; motivo?: string; }
interface Departamento { id: number; nombre: string; }
interface Rol { id: number; nombre: string; }
interface AuditPage { content: Auditoria[]; totalElements: number; totalPages: number; number: number; }

@Component({
  selector: 'app-root', imports: [FormsModule, DatePipe], styleUrl: './app.css', templateUrl: './app.html',
})
export class App implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly api = 'http://localhost:8080/api';
  session: Session | null = null;
  section: Section = 'documentos';
  documents: Documento[] = [];
  users: Usuario[] = [];
  audits: Auditoria[] = [];
  departments: Departamento[] = [];
  roles: Rol[] = [];
  totalAuditCount = 0;
  deviceType = localStorage.getItem('securedocs-device-type') ?? 'CORPORATIVO';
  auditFilters = { usuario: '', accion: '', resultado: '', desde: '', hasta: '' };
  userSearch = '';
  documentStatus = 'TODOS';
  auditPage = 1;
  readonly auditPageSize = 10;
  search = '';
  busy = false;
  error = '';
  notice = '';
  loginCorreo = '';
  loginPassword = '';
  showDocumentForm = false;
  showUserForm = false;
  selectedDocument: Documento | null = null;
  editingUser: Usuario | null = null;
  userDraft = { nombre: '', correo: '', password: '', rolId: 0, departamentoId: 0, nivelSeguridad: 1, pais: 'PERU', tipoContrato: 'INTERNO', estado: 'ACTIVO' };
  editingDocument: Documento | null = null;
  draft = { titulo: '', descripcion: '', departamento: '', nivelConfidencialidad: 1, estado: 'PENDIENTE', pais: 'PERU' };

  ngOnInit(): void {
    const saved = localStorage.getItem('securedocs-session');
    if (saved) {
      try { this.session = JSON.parse(saved) as Session; this.loadSection(); }
      catch { localStorage.removeItem('securedocs-session'); }
    }
  }

  get isAdmin(): boolean { return this.session?.rol?.toLowerCase().includes('admin') ?? false; }
  get isPersonalDevice(): boolean { return this.deviceType === 'PERSONAL'; }
  get canCreate(): boolean { return this.hasRole('administrador', 'gerente', 'supervisor', 'empleado'); }
  get canModify(): boolean { return this.canCreate; }
  get canApprove(): boolean { return this.hasRole('administrador', 'gerente', 'supervisor'); }
  get canManageDocumentState(): boolean { return this.isAdmin; }
  get canDelete(): boolean { return this.hasRole('administrador', 'gerente'); }
  get canManageUsers(): boolean { return this.isAdmin; }
  get canSeeAudit(): boolean { return ['administrador', 'auditor'].some(role => this.session?.rol?.toLowerCase().includes(role)); }
  get filteredDocuments(): Documento[] {
    const q = this.search.trim().toLowerCase();
    return this.documents.filter(d => (this.documentStatus === 'TODOS' || d.estado?.toUpperCase() === this.documentStatus)
      && (!q || [d.titulo, d.descripcion, d.departamento?.nombre, d.estado].some(v => v?.toLowerCase().includes(q))));
  }
  get initials(): string { return (this.session?.nombre ?? 'U').split(/\s+/).slice(0, 2).map(x => x[0]).join('').toUpperCase(); }
  get pendingCount(): number { return this.documents.filter(d => d.estado?.toUpperCase() === 'PENDIENTE').length; }
  get totalAuditPages(): number { return Math.max(1, Math.ceil(this.totalAuditCount / this.auditPageSize)); }
  get pagedAudits(): Auditoria[] { return this.audits; }
  get firstAuditRow(): number { return this.totalAuditCount ? (this.auditPage - 1) * this.auditPageSize + 1 : 0; }
  get lastAuditRow(): number { return Math.min(this.auditPage * this.auditPageSize, this.totalAuditCount); }
  get filteredUsers(): Usuario[] {
    const q = this.userSearch.trim().toLowerCase();
    return this.users.filter(user => !q || [user.nombre, user.correo, user.rol?.nombre, user.departamento?.nombre, user.estado].some(value => value?.toLowerCase().includes(q)));
  }
  private hasRole(...roles: string[]): boolean { const role = this.session?.rol?.toLowerCase() ?? ''; return roles.some(item => role.includes(item)); }

  login(): void {
    this.error = ''; this.busy = true;
    this.http.post<Session>(`${this.api}/auth/login`, { correo: this.loginCorreo, password: this.loginPassword }).subscribe({
      next: session => { this.session = session; localStorage.setItem('securedocs-session', JSON.stringify(session)); this.busy = false; this.changeDetector.markForCheck(); this.loadSection(); },
      error: e => { this.error = this.errorMessage(e, 'No se pudo iniciar sesión. Verifica tus datos y que el backend esté activo.'); this.busy = false; this.changeDetector.markForCheck(); },
    });
  }

  logout(): void { this.session = null; localStorage.removeItem('securedocs-session'); this.documents = []; this.users = []; this.audits = []; this.error = ''; }
  toggleDeviceType(): void {
    this.deviceType = this.deviceType === 'CORPORATIVO' ? 'PERSONAL' : 'CORPORATIVO';
    localStorage.setItem('securedocs-device-type', this.deviceType);
    this.notice = `Contexto de demostración: dispositivo ${this.deviceType.toLowerCase()}.`;
  }
  selectSection(section: Section): void { this.section = section; this.search = ''; this.auditPage = 1; this.loadSection(); }
  changeAuditPage(page: number): void { this.auditPage = Math.min(Math.max(page, 1), this.totalAuditPages); this.loadAuditPage(); }
  applyAuditFilters(): void { this.auditPage = 1; this.loadAuditPage(); }
  private loadAuditPage(): void {
    let params = new HttpParams().set('page', String(this.auditPage - 1)).set('size', String(this.auditPageSize));
    for (const [key, value] of Object.entries(this.auditFilters)) if (value) params = params.set(key, value);
    this.busy = true;
    this.http.get<AuditPage | Auditoria[]>(`${this.api}/auditoria`, { ...this.options(), params }).subscribe({
      next: result => {
        if (Array.isArray(result)) {
          // Compatibilidad mientras Spring Boot no se reinicie con el endpoint paginado.
          this.totalAuditCount = result.length;
          const start = (this.auditPage - 1) * this.auditPageSize;
          this.audits = result.slice(start, start + this.auditPageSize);
        } else {
          this.audits = result.content ?? [];
          this.totalAuditCount = result.totalElements ?? this.audits.length;
        }
        this.busy = false;
        this.changeDetector.markForCheck();
      },
      error: e => { this.busy = false; this.error = this.errorMessage(e, 'No se pudo cargar la auditoría.'); this.changeDetector.markForCheck(); },
    });
  }
  loadSection(): void {
    this.error = ''; this.notice = '';
    if (this.section === 'documentos') this.request<Documento[]>('/documentos', data => this.documents = data);
    if (this.section === 'usuarios' && this.canManageUsers) this.request<Usuario[]>('/usuarios', data => this.users = data);
    if (this.section === 'auditoria' && this.canSeeAudit) this.loadAuditPage();
  }
  openCreate(): void { this.editingDocument = null; this.draft = { titulo: '', descripcion: '', departamento: '', nivelConfidencialidad: 1, estado: 'PENDIENTE', pais: 'PERU' }; this.showDocumentForm = true; this.loadDepartments(); }
  openEdit(doc: Documento): void { this.editingDocument = doc; this.draft = { titulo: doc.titulo, descripcion: doc.descripcion ?? '', departamento: String(doc.departamento?.id ?? ''), nivelConfidencialidad: doc.nivelConfidencialidad, estado: doc.estado, pais: doc.pais }; this.showDocumentForm = true; this.loadDepartments(); }
  openDocumentDetail(doc: Documento): void { this.selectedDocument = doc; }
  private loadDepartments(): void {
    this.http.get<Departamento[]>(`${this.api}/catalogos/departamentos`, this.options()).subscribe({ next: values => { this.departments = values; if (!this.draft.departamento && values.length) this.draft.departamento = String(values[0].id); this.changeDetector.markForCheck(); }, error: e => { this.error = this.errorMessage(e, 'No se pudieron cargar los departamentos.'); this.changeDetector.markForCheck(); } });
  }
  openNewUser(): void {
    this.editingUser = null;
    this.userDraft = { nombre: '', correo: '', password: '', rolId: this.roles[0]?.id ?? 0, departamentoId: this.departments[0]?.id ?? 0, nivelSeguridad: 1, pais: 'PERU', tipoContrato: 'INTERNO', estado: 'ACTIVO' };
    this.showUserForm = true;
    this.loadUserCatalogs();
  }
  openEditUser(user: Usuario): void {
    this.editingUser = user;
    this.userDraft = { nombre: user.nombre, correo: user.correo, password: '', rolId: user.rol?.id ?? 0, departamentoId: user.departamento?.id ?? 0, nivelSeguridad: user.nivelSeguridad, pais: user.pais, tipoContrato: user.tipoContrato, estado: user.estado };
    this.showUserForm = true;
    this.loadUserCatalogs();
  }
  private loadUserCatalogs(): void {
    this.http.get<Departamento[]>(`${this.api}/catalogos/departamentos`, this.options()).subscribe({ next: values => { this.departments = values; if (!this.userDraft.departamentoId) this.userDraft.departamentoId = values[0]?.id ?? 0; this.changeDetector.markForCheck(); }, error: e => { this.error = this.errorMessage(e, 'No se pudieron cargar los departamentos.'); this.changeDetector.markForCheck(); } });
    this.http.get<Rol[]>(`${this.api}/catalogos/roles`, this.options()).subscribe({ next: values => { this.roles = values; if (!this.userDraft.rolId) this.userDraft.rolId = values[0]?.id ?? 0; this.changeDetector.markForCheck(); }, error: e => { this.error = this.errorMessage(e, 'No se pudieron cargar los roles.'); this.changeDetector.markForCheck(); } });
  }
  saveUser(): void {
    const payload: Record<string, unknown> = { nombre: this.userDraft.nombre, correo: this.userDraft.correo, rol: { id: Number(this.userDraft.rolId) }, departamento: { id: Number(this.userDraft.departamentoId) }, nivelSeguridad: Number(this.userDraft.nivelSeguridad), pais: this.userDraft.pais, tipoContrato: this.userDraft.tipoContrato, estado: this.userDraft.estado };
    if (this.userDraft.password) payload['password'] = this.userDraft.password;
    const request = this.editingUser
      ? this.http.put<Usuario>(`${this.api}/usuarios/${this.editingUser.id}`, payload, this.options())
      : this.http.post<Usuario>(`${this.api}/usuarios`, { ...payload, password: this.userDraft.password }, this.options());
    this.busy = true; this.error = '';
    request.subscribe({ next: () => { this.busy = false; this.showUserForm = false; this.notice = this.editingUser ? 'Usuario actualizado.' : 'Usuario creado.'; this.changeDetector.markForCheck(); this.loadSection(); }, error: e => { this.busy = false; this.error = this.errorMessage(e, 'No se pudo guardar el usuario.'); this.changeDetector.markForCheck(); } });
  }
  toggleUserState(user: Usuario): void {
    const state = user.estado?.toUpperCase() === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
    const payload = { nombre: user.nombre, correo: user.correo, rol: user.rol, departamento: user.departamento, nivelSeguridad: user.nivelSeguridad, pais: user.pais, tipoContrato: user.tipoContrato, estado: state };
    this.http.put<Usuario>(`${this.api}/usuarios/${user.id}`, payload, this.options()).subscribe({ next: () => { this.notice = `Usuario ${state.toLowerCase()}.`; this.changeDetector.markForCheck(); this.loadSection(); }, error: e => { this.error = this.errorMessage(e, 'No se pudo cambiar el estado del usuario.'); this.changeDetector.markForCheck(); } });
  }
  saveDocument(): void {
    const payload: Record<string, unknown> = { titulo: this.draft.titulo, descripcion: this.draft.descripcion, departamento: { id: Number(this.draft.departamento) }, nivelConfidencialidad: Number(this.draft.nivelConfidencialidad), estado: this.draft.estado, pais: this.draft.pais };
    const request = this.editingDocument ? this.http.put<Documento>(`${this.api}/documentos/${this.editingDocument.id}`, payload, this.options()) : this.http.post<Documento>(`${this.api}/documentos`, payload, this.options());
    this.busy = true; this.error = '';
    request.subscribe({ next: () => { this.busy = false; this.showDocumentForm = false; this.notice = this.editingDocument ? 'Documento actualizado.' : 'Documento creado.'; this.changeDetector.markForCheck(); this.loadSection(); }, error: e => { this.busy = false; this.error = this.errorMessage(e, 'No se pudo guardar el documento.'); this.changeDetector.markForCheck(); } });
  }
  approve(doc: Documento): void {
    this.http.post<Documento>(`${this.api}/documentos/${doc.id}/aprobar`, {}, this.options()).subscribe({ next: updated => { this.notice = updated.estado?.toUpperCase() === 'PUBLICADO' ? 'Documento aprobado y publicado para consulta externa.' : 'Documento aprobado.'; this.changeDetector.markForCheck(); this.loadSection(); }, error: e => { this.error = this.errorMessage(e, 'No se pudo aprobar el documento.'); this.changeDetector.markForCheck(); } });
  }
  changeDocumentState(doc: Documento, action: 'publicar' | 'retirar-publicacion' | 'reabrir'): void {
    this.http.post<Documento>(`${this.api}/documentos/${doc.id}/${action}`, {}, this.options()).subscribe({
      next: updated => { this.notice = `Estado actualizado: ${updated.estado}.`; this.changeDetector.markForCheck(); this.loadSection(); },
      error: e => { this.error = this.errorMessage(e, 'No se pudo cambiar el estado del documento.'); this.changeDetector.markForCheck(); },
    });
  }
  remove(doc: Documento): void {
    if (!confirm(`¿Eliminar “${doc.titulo}”?`)) return;
    this.http.delete(`${this.api}/documentos/${doc.id}`, this.options()).subscribe({ next: () => { this.notice = 'Documento eliminado.'; this.changeDetector.markForCheck(); this.loadSection(); }, error: e => { this.error = this.errorMessage(e, 'No se pudo eliminar el documento.'); this.changeDetector.markForCheck(); } });
  }
  private request<T>(path: string, assign: (data: T) => void): void {
    this.busy = true;
    this.http.get<T>(`${this.api}${path}`, this.options()).subscribe({ next: data => { assign(data); this.busy = false; this.changeDetector.markForCheck(); }, error: e => { this.busy = false; this.error = this.errorMessage(e, 'No se pudieron cargar los datos.'); this.changeDetector.markForCheck(); } });
  }
  private options() { return { headers: { Authorization: `${this.session?.tipo || 'Bearer'} ${this.session?.token ?? ''}`, 'X-Device-Type': this.deviceType } }; }
  private errorMessage(error: HttpErrorResponse, fallback: string): string {
    if (error.status === 0) return 'No hay conexión con el backend. Comprueba que Spring Boot esté activo en localhost:8080.';
    if (typeof error.error === 'string' && error.error.trim()) return error.error;
    if (error.status === 401) return 'Tu sesión venció. Inicia sesión nuevamente.';
    if (error.status === 403) return 'Tu rol no tiene permiso para realizar esta acción.';
    return fallback;
  }
}
