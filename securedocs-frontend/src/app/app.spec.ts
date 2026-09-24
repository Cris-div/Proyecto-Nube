import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { App } from './app';

describe('SecureDocs app', () => {
  let http: HttpTestingController;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('renders the Spanish sign-in screen', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('h2')?.textContent).toContain('Inicia sesión');
    expect(fixture.nativeElement.querySelector('input[type="email"]')).not.toBeNull();
  });

  it('stores the session after a successful login and loads visible documents', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    app.loginCorreo = 'admin@securedocs.com';
    app.loginPassword = '123456';

    app.login();

    const login = http.expectOne('http://localhost:8080/api/auth/login');
    expect(login.request.method).toBe('POST');
    expect(login.request.body).toEqual({ correo: 'admin@securedocs.com', password: '123456' });
    login.flush({ token: 'test-token', tipo: 'Bearer', usuarioId: 1, nombre: 'Admin', correo: 'admin@securedocs.com', rol: 'Administrador' });

    const documents = http.expectOne('http://localhost:8080/api/documentos');
    expect(documents.request.headers.get('Authorization')).toBe('Bearer test-token');
    expect(documents.request.headers.get('X-Device-Type')).toBe('CORPORATIVO');
    documents.flush([]);

    expect(app.session?.correo).toBe('admin@securedocs.com');
    expect(app.busy).toBe(false);
    expect(JSON.parse(localStorage.getItem('securedocs-session') ?? '{}').token).toBe('test-token');
  });

  it('shows legacy audit lists and paged audit responses', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    app.session = { token: 'test-token', tipo: 'Bearer', usuarioId: 1, nombre: 'Admin', correo: 'admin@securedocs.com', rol: 'Administrador' };
    app.selectSection('auditoria');

    const legacyRequest = http.expectOne(request => request.url === 'http://localhost:8080/api/auditoria' && request.params.get('page') === '0');
    const legacyRecords = Array.from({ length: 11 }, (_, index) => ({ id: index + 1, recurso: `DOCUMENTO ${index + 1}`, accion: 'CONSULTAR', fecha: '2026-09-23T10:00:00', resultado: 'PERMITIDO' }));
    legacyRequest.flush(legacyRecords);
    expect(app.totalAuditCount).toBe(11);
    expect(app.audits).toHaveLength(10);

    app.changeAuditPage(2);
    const pageRequest = http.expectOne(request => request.url === 'http://localhost:8080/api/auditoria' && request.params.get('page') === '1');
    pageRequest.flush({ content: [legacyRecords[10]], totalElements: 11, totalPages: 2, number: 1 });
    expect(app.audits.map(item => item.id)).toEqual([11]);
    expect(app.totalAuditPages).toBe(2);
  });

  it('sends the selected device context to document authorization', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    app.session = { token: 'test-token', tipo: 'Bearer', usuarioId: 1, nombre: 'Admin', correo: 'admin@securedocs.com', rol: 'Administrador' };
    app.deviceType = 'CORPORATIVO';
    app.toggleDeviceType();
    app.selectSection('documentos');

    const request = http.expectOne('http://localhost:8080/api/documentos');
    expect(request.request.headers.get('X-Device-Type')).toBe('PERSONAL');
    request.flush([]);
  });
});
