# Guía de Convenciones del Proyecto

## 📝 Estrategia de Commits (Micro-Commits)

Este proyecto sigue la convención **[Conventional Commits](https://www.conventionalcommits.org/)** para mantener un historial claro, legible y automatizable.

El objetivo es realizar **micro-commits**: cambios pequeños y atómicos que hacen una sola cosa bien.

### Formato del Mensaje
`<tipo>: <descripción breve en minúsculas>`

Ejemplo: `feat: add Product entity`

### Glosario de Tipos

| Tipo | Uso Principal | Ejemplos |
| :--- | :--- | :--- |
| **`feat`** | Una nueva funcionalidad para el usuario. | Añadir un endpoint, crear una nueva entidad, nueva lógica de negocio. |
| **`fix`** | Corrección de un bug. | Corregir un NPE, arreglar una validación que fallaba, solucionar un error 500. |
| **`chore`** | Tareas rutinarias que no afectan el código de producción. | Actualizar dependencias, cambios en `.gitignore`, configuración de Docker/IDE. |
| **`docs`** | Cambios solo en la documentación. | Actualizar el README, correcciones en DESIGN.md, Javadoc. |
| **`style`** | Cambios de formato que no afectan la lógica. | Espacios en blanco, punto y coma faltante, formateo de código (indentación). |
| **`refactor`** | Cambio de código que no arregla un bug ni añade una funcionalidad. | Renombrar variables para claridad, simplificar una función compleja, mover clases. |
| **`test`** | Añadir o corregir pruebas. | Crear tests unitarios, actualizar tests de integración obsoletos. |
| **`perf`** | Cambios que mejoran el rendimiento. | Optimizar una query SQL, eliminar bucles innecesarios. |
| **`build`** | Cambios que afectan el sistema de construcción o dependencias externas. | Cambios en `pom.xml` (Maven) o settings de Gradle. |
| **`ci`** | Cambios en archivos de configuración de CI. | GitHub Actions workflows, Jenkinsfile. |

---
> **Regla de Oro:** Si tienes que usar la palabra "y" en tu mensaje de commit (ej. "feat: add user AND fix login"), probablemente deberías dividirlo en dos micro-commits separados.