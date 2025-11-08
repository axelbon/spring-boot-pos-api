# Java POS API (Spring Boot 3)

> **Transición Profesional a JVM:** Este proyecto sirve como la implementación insignia de mi transición desde 4 años de experiencia como desarrollador Node.js hacia el ecosistema empresarial de Java moderno.

## 📖 Sobre el Proyecto
El objetivo es construir una API RESTful robusta para un sistema de Punto de Venta (POS), aplicando patrones de arquitectura sólidos y buenas prácticas adquiridas en mi experiencia previa, pero utilizando las herramientas punteras del ecosistema JVM actual: **Java 17** y **Spring Boot 3**.

El foco no está solo en "que funcione", sino en que sea mantenible, seguro, escalable y listo para producción.

## 🛠️ Tech Stack
* **Core:** Java 17, Spring Boot 3
* **Datos:** PostgreSQL, Spring Data JPA, Hibernate
* **Seguridad:** Spring Security 6, JWT (JSON Web Tokens)
* **Testing:** JUnit 5, Mockito, H2 Database (para tests de integración)
* **DevOps & CI/CD:** Docker, Jenkins/GitHub Actions, Maven
* **Utilidades:** Lombok, Bean Validation

## ✨ Características Clave
Este no es un CRUD básico. El diseño incluye características avanzadas de negocio:

* **🔐 RBAC Granular:** Control de acceso basado en roles (SUPER_ADMIN, ADMIN, CASHIER) protegiendo cada endpoint.
* **🛒 Transaccionalidad Robusta:** Manejo ACID de ventas complejas, asegurando la consistencia del inventario ante fallos.
* **🛡️ Soft Delete:** Implementación estratégica de borrado lógico para preservar la integridad histórica de los reportes.
* **📊 API Estandarizada:** Endpoints RESTful con soporte nativo para paginación, filtrado dinámico y ordenamiento múltiple.
* **📦 Auditoría de Inventario:** Registro inmutable de todos los movimientos de stock (`StockMovement`) para trazabilidad total.

## 📚 Documentación de Diseño
Antes de escribir una sola línea de código, se definió una especificación completa de la API y el modelo de datos.

👉 **[Ver Especificación de Diseño y API (DESIGN.md)](./docs/DESIGN.md)**

## 🚀 Roadmap
* [ ] **MVP (Fase Actual):** Implementación de módulos core (Auth, Productos, Inventario, Ventas).
* [ ] **V1.5:** Pruebas de integración completas y dockerización del entorno de desarrollo.
* [ ] **V2.0:** Módulo de reportes avanzados y manejo de devoluciones (Cancelaciones con restauración de stock).
* [ ] **Futuro:** Sistema dinámico de permisos desacoplados de los roles.

---
*Desarrollado por @axelbon - 2025*