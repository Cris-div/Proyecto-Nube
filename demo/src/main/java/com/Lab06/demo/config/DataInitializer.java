package com.Lab06.demo.config;

import com.Lab06.demo.entity.*;
import com.Lab06.demo.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final RolPermisoRepository rolPermisoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DocumentoRepository documentoRepository;
    private final PasswordEncoder passwordEncoder;
    private final PoliticaRepository politicaRepository;

    public DataInitializer(
            RolRepository rolRepository,
            PermisoRepository permisoRepository,
            RolPermisoRepository rolPermisoRepository,
            DepartamentoRepository departamentoRepository,
            UsuarioRepository usuarioRepository,
            DocumentoRepository documentoRepository,
            PasswordEncoder passwordEncoder,
            PoliticaRepository politicaRepository) {

        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
        this.rolPermisoRepository = rolPermisoRepository;
        this.departamentoRepository = departamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.documentoRepository = documentoRepository;
        this.passwordEncoder = passwordEncoder;
        this.politicaRepository = politicaRepository;
    }

    @Override
    public void run(String... args) {

        // ==========================================
        // 1. ROLES
        // ==========================================

        Map<String, Rol> roles = new HashMap<>();

        roles.put("Administrador",
                crearRol("Administrador"));

        roles.put("Gerente",
                crearRol("Gerente"));

        roles.put("Supervisor",
                crearRol("Supervisor"));

        roles.put("Empleado",
                crearRol("Empleado"));

        roles.put("Auditor",
                crearRol("Auditor"));

        roles.put("Invitado",
                crearRol("Invitado"));


        // ==========================================
        // 2. PERMISOS
        // ==========================================

        Map<String, Permiso> permisos = new HashMap<>();

        permisos.put(
                "Crear documento",
                crearPermiso("Crear documento")
        );

        permisos.put(
                "Consultar documento",
                crearPermiso("Consultar documento")
        );

        permisos.put(
                "Modificar documento",
                crearPermiso("Modificar documento")
        );

        permisos.put(
                "Eliminar documento",
                crearPermiso("Eliminar documento")
        );

        permisos.put(
                "Aprobar documento",
                crearPermiso("Aprobar documento")
        );

        permisos.put(
                "Ver auditoría",
                crearPermiso("Ver auditoría")
        );

        permisos.put(
                "Gestionar usuarios",
                crearPermiso("Gestionar usuarios")
        );

        permisos.put(
                "Asignar roles",
                crearPermiso("Asignar roles")
        );

        permisos.put(
                "Gestionar estado documento",
                crearPermiso("Gestionar estado documento")
        );


        // ==========================================
        // 3. PERMISOS ADMINISTRADOR
        // ==========================================

        asignar(
                roles.get("Administrador"),
                permisos.get("Crear documento")
        );

        asignar(
                roles.get("Administrador"),
                permisos.get("Consultar documento")
        );

        asignar(
                roles.get("Administrador"),
                permisos.get("Modificar documento")
        );

        asignar(
                roles.get("Administrador"),
                permisos.get("Eliminar documento")
        );

        asignar(
                roles.get("Administrador"),
                permisos.get("Aprobar documento")
        );

        asignar(
                roles.get("Administrador"),
                permisos.get("Ver auditoría")
        );

        asignar(
                roles.get("Administrador"),
                permisos.get("Gestionar usuarios")
        );

        asignar(
                roles.get("Administrador"),
                permisos.get("Asignar roles")
        );

        asignar(
                roles.get("Administrador"),
                permisos.get("Gestionar estado documento")
        );


        // ==========================================
        // 4. PERMISOS GERENTE
        // ==========================================

        asignar(
                roles.get("Gerente"),
                permisos.get("Crear documento")
        );

        asignar(
                roles.get("Gerente"),
                permisos.get("Consultar documento")
        );

        asignar(
                roles.get("Gerente"),
                permisos.get("Modificar documento")
        );

        asignar(
                roles.get("Gerente"),
                permisos.get("Eliminar documento")
        );

        asignar(
                roles.get("Gerente"),
                permisos.get("Aprobar documento")
        );

        // El enunciado reserva la auditoría a Administrador y Auditor.
        // Revocar también limpia la asignación que pudiera persistir en una BD previa.
        revocar(roles.get("Gerente"), permisos.get("Ver auditoría"));


        // ==========================================
        // 5. PERMISOS SUPERVISOR
        // ==========================================

        asignar(
                roles.get("Supervisor"),
                permisos.get("Crear documento")
        );

        asignar(
                roles.get("Supervisor"),
                permisos.get("Consultar documento")
        );

        asignar(
                roles.get("Supervisor"),
                permisos.get("Modificar documento")
        );

        asignar(
                roles.get("Supervisor"),
                permisos.get("Aprobar documento")
        );


        // ==========================================
        // 6. PERMISOS EMPLEADO
        // ==========================================

        asignar(
                roles.get("Empleado"),
                permisos.get("Crear documento")
        );

        asignar(
                roles.get("Empleado"),
                permisos.get("Consultar documento")
        );

        asignar(
                roles.get("Empleado"),
                permisos.get("Modificar documento")
        );


        // ==========================================
        // 7. PERMISOS AUDITOR
        // ==========================================

        asignar(
                roles.get("Auditor"),
                permisos.get("Consultar documento")
        );

        asignar(
                roles.get("Auditor"),
                permisos.get("Ver auditoría")
        );


        // ==========================================
        // 8. PERMISOS INVITADO
        // ==========================================

        asignar(
                roles.get("Invitado"),
                permisos.get("Consultar documento")
        );


        // ==========================================
        // 9. DEPARTAMENTOS
        // ==========================================

        Departamento finanzas =
                crearDepartamento("FINANZAS");

        Departamento rrhh =
                crearDepartamento("RRHH");

        Departamento tecnologia =
                crearDepartamento("TECNOLOGIA");


        // ==========================================
        // 10. USUARIOS DE PRUEBA
        // ==========================================

        crearUsuario(
                "Administrador Principal",
                "admin@securedocs.com",
                "123456",
                roles.get("Administrador"),
                tecnologia,
                5,
                "PERU",
                "INTERNO",
                "ACTIVO"
        );

        crearUsuario(
                "Gerente Finanzas",
                "gerente@securedocs.com",
                "123456",
                roles.get("Gerente"),
                finanzas,
                5,
                "PERU",
                "INTERNO",
                "ACTIVO"
        );

        crearUsuario(
                "Supervisor Finanzas",
                "supervisor@securedocs.com",
                "123456",
                roles.get("Supervisor"),
                finanzas,
                3,
                "PERU",
                "INTERNO",
                "ACTIVO"
        );

        crearUsuario(
                "Empleado Finanzas",
                "empleado@securedocs.com",
                "123456",
                roles.get("Empleado"),
                finanzas,
                2,
                "PERU",
                "INTERNO",
                "ACTIVO"
        );

        crearUsuario(
                "Auditor Principal",
                "auditor@securedocs.com",
                "123456",
                roles.get("Auditor"),
                tecnologia,
                4,
                "PERU",
                "INTERNO",
                "ACTIVO"
        );

        crearUsuario(
                "Invitado Externo",
                "invitado@securedocs.com",
                "123456",
                roles.get("Invitado"),
                finanzas,
                1,
                "PERU",
                "EXTERNO",
                "ACTIVO"
        );

        crearDocumentoPublicoDemo(tecnologia);


        // ==========================================
        // 11. POLÍTICAS ABAC
        // ==========================================

        crearPolitica(
                "Usuario activo",
                "USUARIO_ACTIVO",
                "El usuario debe encontrarse en estado ACTIVO",
                "DENY",
                true
        );

        crearPolitica(
                "Mismo departamento",
                "DEPARTAMENTO_IGUAL",
                "El departamento del usuario debe coincidir con el del documento",
                "DENY",
                true
        );

        crearPolitica(
                "Nivel de seguridad suficiente",
                "NIVEL_SEGURIDAD",
                "El nivel de seguridad del usuario debe ser igual o superior al nivel de confidencialidad",
                "DENY",
                true
        );

        crearPolitica(
                "Propietario para modificar",
                "PROPIETARIO_MODIFICAR",
                "Para modificar un documento el usuario debe ser propietario, salvo Administrador o Gerente",
                "DENY",
                true
        );

        crearPolitica(
                "Horario para información confidencial",
                "HORARIO_CONFIDENCIAL",
                "Los documentos con confidencialidad 4 o superior solo pueden consultarse entre 08:00 y 18:00",
                "DENY",
                true
        );

        crearPolitica(
                "Mismo país",
                "PAIS_IGUAL",
                "El país del usuario debe coincidir con el país del documento",
                "DENY",
                true
        );

        crearPolitica(
                "Dispositivo corporativo",
                "DISPOSITIVO_CORPORATIVO",
                "Los documentos con confidencialidad 4 o superior requieren dispositivo corporativo",
                "DENY",
                true
        );

        crearPolitica(
                "Restricción para invitados",
                "INVITADO_RESTRINGIDO",
                "El invitado debe ser EXTERNO y solo puede acceder a documentos PUBLICADOS con confidencialidad máxima 1",
                "DENY",
                true
        );


        // ==========================================
        // MENSAJE
        // ==========================================

        System.out.println("==========================================");
        System.out.println("DATOS INICIALES CARGADOS CORRECTAMENTE");
        System.out.println("==========================================");

        System.out.println("Usuarios de prueba:");

        System.out.println("admin@securedocs.com");
        System.out.println("gerente@securedocs.com");
        System.out.println("supervisor@securedocs.com");
        System.out.println("empleado@securedocs.com");
        System.out.println("auditor@securedocs.com");
        System.out.println("invitado@securedocs.com");

        System.out.println("Password de todos: 123456");

        System.out.println("==========================================");
    }


    // ==========================================
    // CREAR / BUSCAR ROL
    // ==========================================

    private Rol crearRol(String nombre) {

        return rolRepository
                .findByNombre(nombre)
                .orElseGet(() ->
                        rolRepository.save(
                                new Rol(nombre)
                        )
                );
    }


    // ==========================================
    // CREAR / BUSCAR PERMISO
    // ==========================================

    private Permiso crearPermiso(String nombre) {

        return permisoRepository
                .findByNombre(nombre)
                .orElseGet(() ->
                        permisoRepository.save(
                                new Permiso(nombre)
                        )
                );
    }


    // ==========================================
    // ASIGNAR PERMISO
    // ==========================================

    private void asignar(
            Rol rol,
            Permiso permiso) {

        if (rolPermisoRepository
                .findByRolAndPermiso(rol, permiso)
                .isEmpty()) {

            RolPermiso rolPermiso =
                    new RolPermiso(
                            rol,
                            permiso
                    );

            rolPermisoRepository.save(
                    rolPermiso
            );
        }
    }

    private void revocar(Rol rol, Permiso permiso) {
        rolPermisoRepository.findByRolAndPermiso(rol, permiso)
                .ifPresent(rolPermisoRepository::delete);
    }


    // ==========================================
    // CREAR / BUSCAR DEPARTAMENTO
    // ==========================================

    private Departamento crearDepartamento(
            String nombre) {

        return departamentoRepository
                .findAll()
                .stream()
                .filter(d ->
                        d.getNombre()
                                .equalsIgnoreCase(nombre)
                )
                .findFirst()
                .orElseGet(() ->
                        departamentoRepository.save(
                                new Departamento(nombre)
                        )
                );
    }


    // ==========================================
    // CREAR / ACTUALIZAR USUARIO
    // ==========================================

    private void crearUsuario(
            String nombre,
            String correo,
            String password,
            Rol rol,
            Departamento departamento,
            Integer nivelSeguridad,
            String pais,
            String tipoContrato,
            String estado) {

        Usuario usuario =
                usuarioRepository
                        .findByCorreo(correo)
                        .orElse(null);

        // ==========================================
        // SI EL USUARIO YA EXISTE
        // ==========================================

        if (usuario != null) {

            /*
             * Si la contraseña todavía está en texto plano,
             * la convertimos a BCrypt.
             *
             * Una contraseña BCrypt normalmente comienza
             * con $2a$, $2b$ o $2y$.
             */
            if (usuario.getPassword() == null
                    || !usuario.getPassword().startsWith("$2")) {

                usuario.setPassword(
                        passwordEncoder.encode(password)
                );

                usuarioRepository.save(usuario);

                System.out.println(
                        "Contraseña convertida a BCrypt: "
                                + correo
                );
            }

            return;
        }


        // ==========================================
        // SI EL USUARIO NO EXISTE
        // ==========================================

        usuario = new Usuario();

        usuario.setNombre(nombre);
        usuario.setCorreo(correo);

        // Contraseña protegida con BCrypt
        usuario.setPassword(
                passwordEncoder.encode(password)
        );

        usuario.setRol(rol);
        usuario.setDepartamento(departamento);
        usuario.setNivelSeguridad(nivelSeguridad);
        usuario.setPais(pais);
        usuario.setTipoContrato(tipoContrato);
        usuario.setEstado(estado);

        usuarioRepository.save(usuario);

        System.out.println(
                "Usuario creado: " + correo
        );
    }

    private void crearDocumentoPublicoDemo(Departamento departamento) {
        String titulo = "Guía pública de SecureDocs";
        boolean existe = documentoRepository.findAll().stream()
                .anyMatch(documento -> titulo.equalsIgnoreCase(documento.getTitulo()));
        if (existe) {
            return;
        }

        Usuario administrador = usuarioRepository
                .findByCorreo("admin@securedocs.com")
                .orElse(null);
        if (administrador == null) {
            return;
        }

        Documento documento = new Documento();
        documento.setTitulo(titulo);
        documento.setDescripcion("Documento de demostración disponible para usuarios invitados.");
        documento.setPropietario(administrador);
        documento.setDepartamento(departamento);
        documento.setNivelConfidencialidad(1);
        documento.setEstado("PUBLICADO");
        documento.setPais("PERU");
        documento.setFechaCreacion(LocalDateTime.now());
        documentoRepository.save(documento);
    }


    // ==========================================
    // CREAR / BUSCAR POLÍTICA
    // ==========================================

    private void crearPolitica(
            String nombre,
            String codigoRegla,
            String descripcion,
            String efecto,
            Boolean activa) {

        Politica politica =
                politicaRepository
                        .findByNombre(nombre)
                        .orElse(null);

        // Si no existe, la creamos
        if (politica == null) {

            politica = new Politica(
                    nombre,
                    codigoRegla,
                    descripcion,
                    efecto,
                    activa
            );

            politicaRepository.save(politica);

            System.out.println(
                    "Política creada: " + codigoRegla
            );

            return;
        }

        /*
         * Si ya existe, actualizamos sus datos.
         * Esto permite modificar posteriormente
         * las políticas sin crear duplicados.
         */

        politica.setCodigoRegla(codigoRegla);
        politica.setDescripcion(descripcion);
        politica.setEfecto(efecto);
        politica.setActiva(activa);

        politicaRepository.save(politica);
    }
}
