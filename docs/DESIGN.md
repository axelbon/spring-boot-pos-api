# Proyecto: API para Punto de Venta (POS)

### 1. Resumen del Proyecto

El objetivo es construir una API RESTful para un sistema de Punto de Venta (POS). Esta API manejará la autenticación de usuarios (cajeros y administradores), la gestión del inventario de productos y el procesamiento de transacciones de venta. El sistema debe garantizar la integridad de los datos, especialmente la consistencia del inventario durante las ventas. Manejando una base de datos sql en PostgreSQL, utilizando Java 17 con Spring Boot 3 y usando `soft delete`.
Esto es una primera version que seguira creciendo con el tiempo, por ahora solo seran 3 modulos.

**Stack Tecnológico:**
* **Lenguaje:** Java 17
* **Framework:** Spring Boot 3
* **Base de Datos:** PostgreSQL
* **Seguridad:** Spring Security con JWT
* **Pruebas:** JUnit 5, Mockito, H2

### 2. Módulos Principales (Alcance)

#### A. Módulo de Seguridad y Usuarios
**Propósito:** Controlar el registro de usuarios, el acceso al sistema y definir qué puede hacer cada usuario.
* **Entidades Clave:** `Usuario` (con email/username y password), `Rol` (ADMIN, SUPER_ADMIN, CAJERO).
* **Funcionalidad Principal:**
    * **Autenticación:** Endpoint de `Login` (`/auth/login`) que recibe credenciales y devuelve un token JWT.
    * **Registro (Admin):** Endpoint (`/auth/register`) para que un `ADMIN` pueda crear nuevos usuarios (cajeros/admins).
    * **Autorización:** El sistema protegerá los endpoints basado en roles.
    * **CRUD de Roles:** (`/api/role`) Operaciones completas para Crear, Leer, Actualizar y Borrar roles.

#### B. Módulo de Inventario y Productos
**Propósito:** Administrar el catálogo de productos que se pueden vender.
* **Entidades Clave:** `Producto` (nombre, SKU, precio, stock, proveedor), `Categoria` (nombre) `Proveedor` (nombre, direccion, contacto).
* **Funcionalidad Principal:**
    * **CRUD de Productos:** (`/api/productos`) Operaciones completas para Crear, Leer, Actualizar y Borrar productos.
    * **CRUD de Categorías:** (`/api/categorias`) Operaciones para gestionar las categorías.
    * **CRUD de Proveedores:** (`/api/proveedores`) Operaciones para gestionar los proveedores.
    * **Seguridad:** Todas estas operaciones estarán restringidas solo para usuarios con rol `ADMIN` o `SUPER_ADMIN`.

#### C. Módulo de Transacciones y Ventas
**Propósito:** Es el corazón del negocio. Maneja el acto de registrar una venta y ajustar el inventario.
* **Entidades Clave:** `Venta` (total, fecha, cajero_id), `DetalleVenta` (venta_id, producto_id, cantidad, precio_unitario).
* **Funcionalidad Principal:**
    * **Crear Venta:** (`POST /api/ventas`) Endpoint principal. Recibirá una lista de productos y sus cantidades.
    * **Lógica de Negocio (Transaccional):**
        1.  Validar el token JWT (asegurar que el cajero está logueado).
        2.  Por cada producto en el pedido, verificar que el `stock` solicitado esté disponible.
        3.  Si algo falla (ej. no hay stock), la venta completa debe fallar (rollback).
        4.  Si todo está bien, descontar el `stock` del inventario (módulo de Productos).
        5.  Guardar la `Venta` y sus `DetalleVenta` en la base de datos.
    * **Historial de Ventas:** (`GET /api/ventas`) Endpoint para que un `ADMIN` o `SUPER_ADMIN` pueda consultar todas las ventas realizadas.
        * {
            status
            message
            data: {
                content: [
                    {
                        id
                        cashier: {
                            id
                            name
                            email
                            created_at
                            role: {
                                id
                                name
                                description
                            }
                        },
                        total
                        status
                        payment_method
                        created_at
                        details[
                            {
                                id
                                sale_id
                                product:{
                                    name
                                    SKU
                                    price
                                    stock
                                    supplier:{
                                        name
                                        email
                                        phone_number
                                        address
                                    },
                                    category:{
                                        name
                                        description
                                    },
                                    is_active
                                    created_at
                                },
                                quantity
                                unit_price
                                notes
                                created_at
                            }
                        ]
                    }
                ]
            }
        }

### Desarrollo

#### Entidades
```json
User(
    id Long pk,
    name varchar(255) NOT NULL,
    email varchar(255) NOT NULL UNIQUE,
    password varchar(255) NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp,
    deleted_at timestamp
)
Role(
    int Long PK,
    name varchar(255) NOT NULL UNIQUE,
    description varchar(255),
    created_at timestamp NOT NULL,
    updated_at timestamp,
    deleted_at timestamp
)

User_Role(
    int Long PK,
    user_id Long FK NOT NULL,
    role_id LONG FK NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp,
    deleted_at timestamp
)

Product(
    id Long pk,
    name varchar(255) NOT NULL,
    SKU varchar(255) NOT NULL UNIQUE,
    price NUMERIC(12, 2) NOT NULL,
    stock int NOT NULL,
    supplier_id Long fk NOT NULL,
    category_id Long fk NOT NULL,
    is_active boolean NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp,
    deleted_at timestamp
)

Stock_Movement(
    int Long PK,
    product_id Long fk NOT NULL,
    user_id Long fk NOT NULL,
    quantity int NOT NULL,
    reason varchar(255),
    created_at timestamp NOT NULL,
)

Supplier(
    id Long pk,
    name varchar(255) NOT NULL UNIQUE,
    email varchar(255) NOT NULL,
    phone_number varchar(255) NOT NULL,
    address varchar(255),
    created_at timestamp NOT NULL,
    updated_at timestamp,
    deleted_at timestamp
)

Category(
    id Long pk,
    name varchar(255) NOT NULL UNIQUE,
    description varchar(255) NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp,
    deleted_at timestamp
)

Sale(
    id Long pk,
    total NUMERIC(12, 2) NOT NULL,
    cashier_id Long FK NOT NULL,
    status ENUM(PENDING, COMPLETED, CANCELLED, TOTAL_REFUND) NOT NULL,
    payment_method ENUM(CASH, CARD, TRANSFER),
    created_at timestamp NOT NULL,
    updated_at timestamp,
    deleted_at timestamp
)

Sale_Detail(
    id Long PK,
    sale_id Long FK NOT NULL,
    product_id Long FK NOT NULL,
    quantity Int NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    notes TEXT,
    created_at timestamp NOT NULL,
    updated_at timestamp,
    deleted_at timestamp
)

Audit_Log(
    id Long PK,
    auditor_id Long FK NOT NULL "Id del usuario que realizo la accion",
    reason varchar(100),
    action ENUM(CREATION, UPDATE, DELETE),
    created_at timestamp NOT NULL,

    -- Claves foráneas de la entidad afectada (Solo una debe estar llena) --
    category_id Long FK NULLABLE,
    product_id Long FK NULLABLE,
    supplier_id Long FK NULLABLE,
    user_id Long FK NULLABLE "Id del usuario al que se aplica una accion",
    role_id Long FK NULLABLE"Id del Rol al que se aplica una accion"
)
```
#### Estructuras de Respuesta Comunes

* **Error de Validación (400 Bad Request)**
    Se devuelve cuando la sintaxis de la entrada (DTO) es incorrecta.
    ```json
    {
    "status": 400,
    "message": "Invalid input for one or more validations",
    "details": [
        {
        "field": "(nombre del campo)",
        "issue": "(descripcion del error)"
        }
    ]
    }
    ```
* **403 Forbidden**
    ```json
    {
        "status": 403,
        "message": "Forbidden: You don't have permissions to access this resource."
    }
    ```
* **401 Unauthorized**
    ```json
    {
        "status": 401,
        "message": "Unauthorized: Access is denied due to invalid credentials"
    }
    ```
#### Endpoints
* **Modulo de seguridad, usuarios, roles**
    * `POST /api/auth/register` ✅
    * `POST /api/auth/login` ✅
    * `GET /api/user` ✅
    * `GET /api/user/{user_id}` ✅
    * `PUT /api/user/{user_id}` ✅
    * `DELETE /api/user/{user_id}` ✅
    * `POST /api/role` ✅
    * `GET /api/role` ✅
    * `GET /api/role/{role_id}` ✅
    * `PUT /api/role/{role_id}` ✅
    * `DELETE /api/role/{role_id}` ✅
* **Modulo de Inventario y producto (categoria, proveedor)**
    * **producto**
    * `GET /api/product` ✅
    * `GET /api/product/{product_id}` ✅
    * `POST /api/product/` `ADMIN`,`SUPER_ADMIN` ✅
    * `PUT /api/product/{product_id}` `ADMIN`,`SUPER_ADMIN` ✅
    * `POST /api/product/{product_id}/activate` ✅
    * `POST /api/product/{product_id}/deactivate` ✅
    * `DELETE /api/product/{product_id}` `ADMIN`,`SUPER_ADMIN` ✅
    * `POST /api/product/{product_id}/stock` `ADMIN`,`SUPER_ADMIN` ✅
    * **Filters & sorts**
    * filter=low_stock
    * filter=by_category
    * filter=by_suplier
    * sort = name,asc/desc and created_at,asc/desc
    
    * **proveedor**
    * `GET /api/supplier` ✅
    * `GET /api/supplier/{supplier_id}` ✅
    * `POST /api/supplier` ✅
    * `PUT /api/supplier/{supplier_id}` ✅
    * `DELETE /api/supplier/{supplier_id}` ✅

    * **categoria**
    * `GET /api/category` ✅
    * `GET /api/category/{category_id}` ✅
    * `POST /api/category` ✅
    * `PUT /api/category/{category_id}` ✅
    * `DELETE /api/category/{category_id}` ✅
* **Modulo de Transacciones y Ventas**
    * `POST /api/sale` ✅
    * `GET /api/sale` ✅
    * `GET /api/sale/{sale_id}`✅
    * `POST /api/sale/{sale_id}/cancel`✅
    * `POST /api/sale/{sale_id}/total_refund` ✅
    * **Filters & sorts**
    * filter=by_cashier
    * filter=by_product
    * sorts = created_at,asc/desc
#### Endpoints_imp
#### Modulo de seguridad, usuarios y roles
#### `POST /api/auth/register`
* **Descripcion:** Endpoint para el registro de usuarios nuevos con un "Super user" con rol `SUPER_ADMIN`.
* **Headers:**
    "Authorization" : Bearer {JWT_TOKEN}
* **Request body:**
        ```json
        {
        "name": "string",
        "email": "string",
        "password": "string",
        "role_id": "id"
        }
        ```
* **Validaciones:**
        * **Request body:**
            * `name`: no debe ser vacio
            * `email`: no debe ser vacio, debe ser email.
            * `password`: no debe ser vacio, debe tener 8 caracteres minimo, debe tener 1 caracter especial minimo, debe tener 1 numero minimo, debe tener 1 mayuscula minimo.
            * `role_id`: no debe ser vacio
* **Request response:**
        * **201 Exito**
            ```json
            {
                "status": 201,
                "message": "User created successfully",
                "data": {
                    "id": "long",
                    "name": "string",
                    "email": "string",
                    "role": {
                        "id": "long",
                        "name": "string",
                        "description": "string"
                    },
                    "created_at": "string"
                }
            }
        * **409 Conflict**
            ```json
            {
                "status": 409,
                "message": "Conflict error: please try again later"
            }
            ```
        * **Role: 404 Not found**
            ```json
            {
                "status": 404,
                "message": "Not found: The role provide has been not found."
            }
            ```
        * **Ejemplo: Input validation error**
            Ver definicion total en Estructuras de Respuesta Comunes-> Error de Validación (400 Bad Request)
            ```
            "details": [
                {
                    "field": "email",
                    "issue": "Must be a valid email address"
                }
            ]
            ```
* **Funcionamiento:**
    - validaciones de dto
        - Si hay un problema enviar error `Ejemplo: Input validation error` definido en `Request response`.
    1. Verificar que el usuario este autenticado y tiene el rol `SUPER_ADMIN`.
        - Un usuario con rol `SUPER_ADMIN` manda el request al servicio de la API, con el body definido correctamente. El servicio se encargara mediante el token JWT de verificar si el user es- `SUPER_ADMIN` o no, si no lo es regresa el error `403` definido en `Estructuras de Respuesta Comunes`:
        Si lo es, el flujo es el siguiente:
    2. Validar que el `email` no exista.
    3. Si `email` ya existe enviar error `409 Conflict` definido en `Request response`
    4. Validar que el `role_id` exista.
    5. Si `role_id` no existe enviar error `Role: 404 Not found` definido en `Request response`
    6. Iniciar transaccion DB
    7. Hashear constrasena con Bcrypt
    8. Crear objeto de usuario
    9. Crear objeto de User_Role
    10. Guardar usuario y User_Role en DB
    11. `COMMIT` transaction DB.
    12. Devolver mensaje de usuario creado `201 Exito` definido en `Request response`.
#### `POST /api/auth/login`
* **Descripcion:** Endpoint para el inicio de sesion de los usuarios.
* **Request body:**
        ```json
            {
                "email": "string",
                "password": "string"
            }
        ```
* **Request response:**
        * **200 success**
        ```json
            {
                "status": 200,
                "message": "Login successfull",
                "jwt_token": {JWT_TOKEN}
            }
        ```
        * **401 Unauthorized**
        ```json
        {
                "status": 401,
                "message": "Email or password is incorrect, please try again"
        }
        ```
* **Validaciones:**
    * **Request body:**
        * `email`: no debe ser vacio, debe ser email
        * `password`: no debe ser vacio
* **Funcionamiento:**
    - validaciones de dto
        - Si hay un problema enviar error `Ejemplo: Input validation error` definido en `Request response`.
    1. Validar existencia de usuario.
    2. Si usuario no existe `401 Unauthorized` definido en `Request response`.
    3. Utilizar Bcrypt para comparar las contrasenas.
    4. Si la contrasena es incorrecta enviar error  `401 Unauthorized` definido en `Request response`.
    5. Crear el `JWT_TOKEN`.
    6. Responder con un mensaje de success con el `JWT_TOKEN` incluido, `200 success` definido en `Request response`
#### `GET /api/users`
* **Descripcion:** Endpoint para listar los usuarios.
* **Headers:**
    "Authorization" : "Bearer {JWT_TOKEN}"
* **Request body:**
    EMPTY
* **Query parameters:**
        * **`pagination`: ej."?page=0&size=10&sort=name,(asc/desc)&sort=created_at,(asc/desc)"**
            - paginacion
                - `page?`: numero de la pagina a pedir
                - `size?`: total de los datos que se buscan por pagina
            - sort
                - `name`: valores `asc` o `desc`
                - created_at: valores `asc` o `desc`
* **Request response:**
    * **200 Successfull**
    ```json
    {
        "status": 200,
        "message": "successfull",
        "data": [
            {
                "id": "long",
                "name": "string",
                "email": "string",
                "created_at": "timestamp",
                "role": {
                    "id": "long",
                    "name": "string",
                    "description": "string"
                }
            }
        ],
        "meta": {
            "currentPage": "long",
            "totalPages": "long",
            "totalElements": "long",
            "pageSize": "long",
            "first": "boolean",
            "last": "boolean"
        }
    }
    ```
* **Validaciones:**
    EMPTY
* **Funcionamiento:**
    1. Mediante el `JWT_TOKEN` validar si es un usuario `ADMIN` o `SUPER_ADMIN`.
    2. Si no tiene permisos regresar respuesta de error `403 Forbidden` definida en `Estructuras de Respuesta Comunes`.
    3. Leer query parameters, o establecer valores por defecto `page=0` y `size=10`.
    4. Hacer consulta a base de datos con el conteo total de usuarios.
    5. Hacer consulta a base de datos con los query parameters por defecto o en su dado caso cuando esten especificados al igual que los sorts declarados en `Query parameters`.
    6. Construir objeto de return y metadata.
    7. Responder con la respuesta `200 Successfull` definida en `Request response`.
#### `GET /api/user/{user_id}`
* **Descripcion:** Endpoint para listar datos de un usuario en especifico seleccionado con el `user_id` proporcionado en path variables.
* **Headers:**
    "Authorization" : Bearer {JWT_TOKEN}
* **Request body:**
    EMPTY
* **Query parameters:**
    EMPTY
* **Path variables:**
        * **User ID:**
                - El parametro `user_id` debe ser de tipo `long`, que sirve para identificar y obtener los datos en la base de datos.
* **Request response:**
        * **200 Successfull**
        ```json
        {
            "status": 200,
            "message": "Successfull",
            "data": {
                "id": "long",
                "name": "string",
                "email": "string",
                "created_at": "timestamp",
                "role": {
                    "id": "long",
                    "name": "string",
                    "description": "string"
                }
            }
        }
        ```

        * **404 Not Found**
        ```json
        {
            "status": 404,
            "message": "Not found: User not found with provided id"
        }
        ```
* **Validaciones:**
        * **Path variables**
            - `user_id`: debe ser un valor entero
* **Funcionamiento:**
    - Validacion de `user_id` debe ser valor entero.
        - Si hay un problema enviar error `Input validation error` definido en `Estructuras de Respuesta Comunes`.
    1. Hacer validacion de usuario mediante JWT proporcionado: un usuario puede obtener sus propios datos pero no los de otro usuario, si no es el mismo `user_id` entonces revisar si es un`ADMIN` o `SUPER_ADMIN`, de no ser correcto esto, enviar mensaje de error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
    2. Hacer consulta a la base de datos con el `user_id` proporcionado.
    3. Si no existe en la base de datos responder con error `404 Not found` definido en `Request response`.
    4. Si la respuesta de la consulta es exitosa y existe el usuario responder con mensaje `200 Successfull` definido en `Request response`.
#### `PUT /api/user/{user_id}`
* **Descripcion:** Endpoint para editar datos de un usuario especifico con el `user_id` proporcionado en path variables, un usuario puede editar sus datos `nombe`, `email` y `password`. Pero un `SUPER_ADMIN` puede cambiar todo eso incluyendo el `role_id` en la entidad `User_Role`.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
* **Request body:**
    * **Body de `SUPER_ADMIN`**
    ```json
    {
        "name": "string",
        "email": "string",
        "password": "string",
        "role_id": "long"
    }
    ```
    * **Body de un `CAJERO` o `ADMIN`**
    ```json
    {
        "name": "string",
        "email": "string",
        "password": "string"
    }
    ```
* **Query parameters:**
    EMPTY
* **Path variable:**
        * **User ID**
            - La variable `user_id` debe ser de tipo `long` para identificar al usuario a editar en la base de datos.
* **Request response:**
        * **200 Successfull**
        ```json
        {
            "status": 200,
            "message": "User edited successfully",
            "data": {
                "id": "long",
                "name": "string",
                "email": "string",
                "created_at": "timestamp",
                "role": {
                    "id": "long",
                    "name": "string",
                    "description": "string"
                }
            }
        }
        ```
        * **404 Not Found**
        ```json
        {
            "status": 404,
            "message": "Not found: User not found with provided id"
        }
        ```
        * **409 Conflict**
        ```json
        {
            "status": 409,
            "message": "Conflict: Unable to update user due to a data conflict."
        }
        ```
* **Validaciones:**
        * **Path variables:**
            * `user_id`: debe ser entero(long), no debe ser vacio
        * **Request body:**
            * `name`: no debe ser vacio
            * `email`: no debe ser vacio, debe ser email
            * `password`: no debe ser vacio, debe tener 8 caracteres minimo, debe tener 1 caracter especial minimo, debe tener 1 numero minimo, debe tener 1 mayuscula minimo.
            * `role_id`: no debe ser vacio(esta bien no incluirlo), debe ser numero entero
* **Funcionamiento:**
        - validaciones de dto
            - Si hay un problema enviar error `Ejemplo: Input validation error` definido en `Request response`.
        1. Validar que el usuario autenticado sea el mismo que el `user_id` en los Path variables.
        2. Validar si el usuario autenticado no es el mismo validar que sea un `ADMIN` o `SUPER_ADMIN`, si no, enviar error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
        3. Validar si el usuario existe en la base de datos. Si no, enviar error `404 Not found` definido en `Request response`.
        4. Validar si el usuario autenticado es `ADMIN` validar que el usuario a editar no sea un `SUPER_ADMIN`, si no, enviar error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
        5. Si el Request body incluye `role_id` validar que el usuario sea `SUPER_ADMIN`, si no, enviar error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
        6. Validar si el email incluid en request body no existe en la base de datos con una consulta como esta `SELECT * FROM users WHERE email = ? AND id != ?`.
        7. Si la validacion de email es positiva(si existe), enviar error `409 Conflict` definido en `Request response`.
        8. Construir nuevo objeto de usuario con los valores nuevo.
        9. Guardar usuario editado en la base de datos.
        10. Responde con mesaje `200 Successfull` definido en `Request response`
#### `DELETE /api/user/{user_id}`
* **Descripcion:** Endpoint para eliminar usuario, esto tambien debe contemplar la eliminacion encascada hacia `User_Role`.
* **Headers:**
    "Authorization" : Bearer {JWT_TOKEN}
* **Request body:**
    EMPTY
* **Query parameters:**
    EMPTY
* **Path variable:**
        * **User ID**
            - La variable `user_id` debe ser de tipo `long` para identificar al usuario a eliminar en la base de datos.
* **Request response:**
    * **200 Successfull:**
    ```json
        {
            "status": 200,
            "message": "Successfull: User deleted succssfully".
        }
    ```
    * **404 Not found:**
    ```json
        {
            "status": 200,
            "message": "Not found: user not found with provided `user_id`." 
        }
    ```
* **Validaciones:**
        * **Path variables:**
            * `user_id`: debe ser entero(long), no debe ser vacio
* **Funcionamiento:**
    - validaciones de `user_id` en Path variables
        - Si hay un problema enviar error `Ejemplo: Input validation error` definido en `Request response`.
    1. Validar si el usuario autenticado tiene rol `SUPER_ADMIN`
        - Si no es `SUPER_ADMIN`, enviar mensaje de error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
    2. Validar que el usuario autenticado y el `user_id` sean diferentes(un usuario `SUPER_ADMIN` no puede eliminarse a si mismo).
        - Si no es, enviar mensaje de error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
    3. Validar que el usuario a eliminar exista
        - Si no existe enviar mensaje de error `404 Not found` definido en `Request response`.
    4. Hacer eliminacion de usuario en base de datos usando `soft delete`.
    5. Buscar y hacer `soft delete` de `User_Role`.
    6. Enviar mensaje `200 Successfull` definido en `Request response`.
#### `POST /api/auth/role`
* **Descripcion:** Endpoint para la creacion de roles, esto solo lo puede realizar un `SUPER_ADMIN` y debe estar a la par con nuevos requerimientos al codigo para andir lasresponsavilidadesdel   rol nuevo.
* **Headers:**
    "Authorization" : Bearer {JWT_TOKEN}
* **Request body:**
        ```json
        {
            "name": "string",
            "description": "string"
        }
        ```
* **Query parameters:**
    EMPTY
* **Path variable:**
    EMPTY
* **Request response:**
        * **201 Successfull**
        ```json
        {
            "status": 201,
            "message": "Successfull: Role created successfully.",
            "data": {
                "name": "string",
                "description": "string"
            }
        }
        ```
        * **409 Conflict**
        ```json
        {
            "status": 409,
            "message": "Conflict: a rol with the provided name already exists."
        }
        ```
* **Validaciones:**
        * **Request body:**
            * `name`: no debe ser vacio, debe ser string
                - usar este regex para seguir una estructura "^[A-Z][A-Z0-9_]*$", message = "El formato debe ser solo mayúsculas, números y guiones bajos, empezando con una letra".
            * `description`: no debe ser vacio, debe ser string
* **Funcionamiento:**
    - validaciones de dto
        - Si hay un problema enviar error `Error de Validación (400 Bad Request)` definido en `Estructuras de Respuesta Comunes`.
    1. Validar si el usuario autenticado tiene rol `SUPER_ADMIN`
        - Si no responder con mensaje de error `403 Forbiden` definido en `Estructuras de Respuesta Comunes`.
    2. Validar si el `name` es unico haciendo uso de una consulta como `SELECT * FORM ROLE WHERE NAME = 'name'`
        - Si existe responer con mensaje de error `409 Conflict` definido en `Request response`.
    3. Guardar nuevo rol en base de datos.
    4. Responder con mensaje `201 Successfull` definido en `Request response`.
#### `GET /api/role`
* **Descripcion:** Endpoint para listar todos los roles en la base de datos, solo accesible por un `ADMIN` o `SUPER_ADMIN`.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
* **Request body:**
EMPTY
* **Query parameters:**
    * **`pagination`: ej."?page=0&size=10&sort=name,(asc/desc)"**
        - paginacion
            - `page?`: numero de la pagina a pedir
            - `size?`: total de los datos que se buscan por pagina
        - sort
            - `name`: valores `asc` o `desc`
* **Path variable:**
    EMPTY
* **Request response:**
        * **200 Successfull**
        ```json
        {
            "status": 200,
            "message": "successfull",
            "data": [
                {
                    "id": "long",
                    "name": "string",
                    "description": "string",
                }
            ],
            "meta": {
                "currentPage": "long",
                "totalPages": "long",
                "totalElements": "long",
                "pageSize": "long",
                "first": "boolean",
                "last": "boolean"
            }
        }
* **Validaciones:**
    EMPTY
* **Funcionamiento:**
    1. Validar si el usuario es un `ADMIN` o `SUPER_ADMIN`
        - Si no lo es, responder con error de `403 Forbidden` definido en Estructuras de Respuesta Comunes.
    2. Leer query parameters, o establecer valores por defecto `page=0` y `size=10`.
    3. Hacer consulta a base de datos con el conteo total de roles.
    4. Hacer consulta a base de datos con los query parameters por defecto o en su dado caso cuando esten especificados al igual que los sorts declarados en `Query parameters`.
    5. Hacer respuesta `200 Successfull` definido en `Request response`.
#### `GET /api/role/{role_id}`
* **Descripcion:** Endpoint para listar un rol en especifico mediante los Path Variables `role_id`.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
* **Request body:**
EMPTY
* **Query parameters:**
EMPTY
* **Path variable:**
    * **Role ID:**
        - El parametro `role_id` debe ser de tipo entero (`long`), que sirve para identificar y obtener los datos en la base de datos.
* **Request response:**
    * **200 Successfull:**
    ```json
    {
        "status": 200,
        "message": "Successfull: Role found successfully",
        "data": {
            "name": "string",
            "description": "string"
        }
    }
    ```

    * **404 Not found:**
    ```json
    {
        "status": 404,
        "message": "Not found: Role not found with provided `role_id`."
    }
    ```
* **Validaciones:**
    * **Path variable:**
        - `role_id`: debe ser tipo entero, no debe ser vacio
* **Funcionamiento:**
        - validaciones de `role_id`
            - Si hay un problema enviar error `Error de Validación (400 Bad Request)` definido en `Estructuras de Respuesta Comunes`.
        1. Verificar si el usuario autenticado tiene rol `ADMIN` o `SUPER_ADMIN`.
            - Si no es, enviar error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
        2. Hacer consulta a la base de datos con el id proporcionado en Path variables `role_id`.
            - Si no existe, enviar error `404 Not found` definido en `Request response`.
        3. Responder con mensaje `200 Successfull` definido en `Request response`.
#### `PUT /api/role/{role_id}`
* **Descripcion:** Endpoint para editar un role especifico con el `role_id` proporcionado en path variables y con los nuevos datos en Request body.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
* **Request body:**
```json
{
    "name": "string",
    "description":"string"
}
```
* **Query parameters:**
EMPTY
* **Path variable:**
    * **Role ID:**
        - El parametro `role_id` debe ser de tipo entero (`long`), que sirve para identificar y obtener los datos en la base de datos.
* **Request response:**
    * **200 Successfull:**
        ```json
        {
            "status": 200,
            "message": "Successfull: Role updated successfully.",
            "data":{
                "name": "string",
                "description": "string"
            }
        }
        ```
    * **404 Not found:**
    ```json
    {
        "status": 404,
        "message": "Not found: Role not found with provided `role_id`."
    }
    ```
    * **409 Conflict:**
    ```json
    {
        "status": 409,
        "message": "Conflict: Role with provided name already exists."
    }
    ```
* **Validaciones:**
    * **Path variable:**
        - `role_id`: debe ser tipo entero, no debe ser vacio
    * **Request body:**
        - `name`: no debe ser vacio, debe ser string
        - `description`: no debe ser vacio, debe ser string
* **Funcionamiento:**
    - validaciones de `role_id` y validaciones de dto
        - Si hay un problema enviar error `Error de Validación (400 Bad Request)` definido en `Estructuras de Respuesta Comunes`.
    1. Validar si usuario autenticado es `ADMIN` o `SUPER_ADMIN`
        - Si no, responder con error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
    2. Validar si rol existe utilizando el `role_id` proporcionado
        - Si no, responder con error `404 Not found` definido en `Request response`.
    3. Validar si el rol a editar es `SUPER_ADMIN` O `ADMIN`.
        - Si son esos, responder con error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
    4. Validar si el `name` del rol ya existe en la bd (SELECT ... WHERE name = ? AND id != ?)
        - Si existe, responder con error `409 Conflict` definido en `Request response`.
    5. Si el rol existe actualizar el objeto obtenido con los valores que vienen en el request body.
    6. Responder con objeto actualizado usando `200 Successfull` definido en `Request response`.
#### `DELETE /api/role/{role_id}`
* **Descripcion:** Endpoint para eliminar rol especifico mediante el path variable `role_id`
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
* **Request body:**
EMPTY
* **Query parameters:**
EMPTY
* **Path variable:**
    * **Role ID**
        - La variable `role_id` debe ser de tipo `long` para identificar al role a eliminar en la base de datos.
* **Request response:**
    * **200 Successfull:**
    ```json
    {
        "status": 200,
        "message": "Successfull: Role deleted successfully."
    }
    ```
    * **404 Not found:**
    ```json
    {
        "status": 404,
        "message": "Not found: Role not found with provided `role_id`."
    }
    ```
    * **409 Conflict:**
    ```json
    {
        "status": 409,
        "message": "Conflict: Provided role is still assigned to one or more users"
    }
    ```
* **Validaciones:**
    * **Path variable:**
        - `role_id`: debe ser tipo entero, no debe ser vacio
* **Funcionamiento:**
    - validaciones de `role_id`
        - Si hay un problema enviar error `Error de Validación (400 Bad Request)` definido en `Estructuras de Respuesta Comunes`.
    1. Validar si el usuario autenticado es `SUPER_ADMIN`.
        - Si no, enviar mensaje de error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
    2. Validar si rol existe en la base de datos con el `role_id` proporcionado.
        - Si no, responder con error `404 Not found` definido en `Request response`.
    3. Validar si el rol a eliminar no es `ADMIN` o `SUPER_ADMIN`
        - Si si lo son, enviar mesanje de error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
    4. Validar si el role no esta asignado a ningun usuario en la entidad `User_Role`.
        - Si si, responder con error `409 Conflict` definido en `Request response`.
    5. Si si, eliminarlo(haciendo uso de softdelete).
    6. Mandar mensaje `200 Successfully` definido en `Request response`.

#### Modulo de Inventario y producto (categoria, proveedor)
#### `GET /api/product`
* **Descripcion:** Endpoint para listar los productos guardados en la base de datos
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
* **Request body:**
EMPTY
* **Query parameters:**
    * **Pagination:**
        - `page`: Número de la página de resultados que se desea recuperar. El valor predeterminado suele ser 0 (primera página).
        - `size`: Cantidad de elementos a incluir en cada página. El valor predeterminado suele ser 10 o 20.
    * **Filters:**
        - `low_stock`: Filtra los productos que tengan menos stock, quizas 10 sea una buena cantidad minima o 5.
            - if exists in query parameters = true if not = false
        - `by_category`: Filtra por categoria, debe ser un tipo entero `long` para ser buscado en la base de datos.
            - `ej. ?by_category=93`
        - `by_supplier`: Filtra por proveedor, debe ser un tipo entero `long` para ser buscado en la base de datos.
            - `ej. ?by_supplier=12`
        - `search`: Filtra por `nombre`, debe ser un valor tipo `string` para ser buscado en la base de datos.
        - `sku`: Filtra por `SKU` especifico, debe ser valor `string` para ser buscado en la base de datos.
    * **Sorts:**
        - `sort=name`: debe tener un valor `asc` o `desc` para el correcto ordenado, haciendo un ordenamiento de nombre.
        - `sort=created_at`: debe tener un valor `asc` o `desc` para el correcto ordenado por el valor de `timestamp` para saber cual se creo primero o el ultimo.
        - `sort=by_stock`: debe tener un valor `asc` o `desc` para el correcto ordenado, por el valor de stock para saber cual tiene mas o menos stock.
        - `sort=by_price`: debe tenre un valor `asc` o `desc` para el correcto ordenado, por el valor `price`.
* **Path variable:**
EMPTY
* **Request response:**
    * **200: Successfull:**
    ```json
    {
        "status": 200,
        "message": "Successfull",
        "data": {
            "content": [
                {
                    "name": "string",
                    "SKU": "string",
                    "price": "numeric",
                    "stock": "int",
                    "supplier":{
                        "name": "string",
                        "email": "string",
                        "phone_number": "string",
                        "address": "string"
                    },
                    "category":{
                        "name": "string",
                        "description": "string"
                    },
                    "is_active": "boolean",
                    "created_at": "timestamp"
                },
            ],
            "pagination": {
                "page": 0,           // Número de página actual
                "size": 10,          // Tamaño de página solicitado
                "totalElements": 50, // Total de elementos en todas las páginas
                "totalPages": 5,     // Total de páginas disponibles
                "last": false        // Indica si es la última página
            }
        }
    }
    ```
* **Validaciones:**
    * **Pagination:**
        - `page`: debe ser un numero entero positivo.
        - `size`: debe ser un numero entero positivo.   
    * **Filters:**
        - `filter=low_stock`: debe existir con el unico motivo de tener un llamado a la base de datos para saber que productos tienen un stock muy bajo.
        - `by_category`: debe ser un numero entero, debe existir cuando se usa el servicio con la intencion de obtener los productos de una unica categoria.
        - `by_supplier`: debe ser un numero entero, debe existir cuando se usa el servicio con la intencion de obtener los productos de un unico proveedor.
        - `search`: debe ser un valor string, debe existir cuando se usa el servicio con la intencion de obtener los productos que tienen un nombre unico.
        - `sku`: debe ser un valor `string`, debe existir cuando se una en el servicio con la intencio de obtener los datos de un producto especifico.
    * **Sorts:**
        - `sort=name`: debe tener un valor `asc` o `desc` para ordenamiento.
        - `sort=created_at`: debe tener un valor `asc` o `desc` para ordenamiento.
        - `sort=by_stock`: debe tener un valor `asc` o `desc` para ordenamiento.
        - `sort=by_price`: debe tener un valor `asc` o `desc` para ordenamiento.
* **Funcionamiento:**
    - Validaciones de sorts y filters
        - Si hay algun problema con filtros o sorts, responder con error `400 Bad Request` definido en `Estructuras de Respuesta Comunes`
    1. Si hay filtros o sorts, hacer las validaciones y acciones correctas para incluirlas en la consulta a la base de datos.
    2. Leer query parameters, o establecer valores por defecto `page=0` y `size=10`.
    3. Hacer consulta a base de datos con el conteo total de productos para mandarlo en la respuesta.
    4. Hacer consulta a la base de datos con la query creada con o sin filtros.
    5. Responder con mensaje `200 Successfull` definido en `Request response`.
#### `GET /api/product/{product_id}`
* **Descripcion:** Endpoint para listar los datos de un producto en especifico a travez del path variable `product_id`.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
* **Request body:**
EMPTY
* **Query parameters:**
EMPTY
* **Path variable:**
    * **Product ID:** El parametro debe ser numero entero y positivo.
* **Request response:**
    * **200 Successfull:**
    ```json
    {
        "status": 200,
        "message": "Successfull",
        "data":{
            "name": "string",
            "SKU": "string",
            "price": "numeric",
            "stock": "int",
            "supplier":{
                "name": "string",
                "email": "string",
                "phone_number": "string",
                "address": "string"
            },
            "category":{
                "name": "string",
                "description": "string"
            },
            "is_active": "boolean",
            "created_at": "timestamp"
        }
    }
    ```
    * **404 Not found:**
    ```json
    {
        "status": 404,
        "message": "Not found: Product not found with provided `product_id`."
    }
    ```
* **Validaciones:**
    * **Path variable:**
        - `product_id`: debe ser numerico entero y positivo.
* **Funcionamiento:**
    - Validaciones de path variable `product_id`
        - Si hay algun problema con el `product_id`, responder con error `400 Bad Request` definido en `Estructuras de Respuesta Comunes`.
    1. Validar si el producto existe consultando a la base de datos con el `product_id` proporcionado, esta validacion sera si existe en la base de datos o si esta "eliminado".
        - Si no, regresar mensaje de error `404 Not found` definido en `Request response`.
    2. Responder con mensaje `200 Successfull` definido en `Request response`.
#### `POST /api/product/`
* **Descripcion:** Endpoint para anadir un producto a la base de datos.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
"X-AUDIT-REASON": (Opcional) Razón para la actualización.
* **Request body:**
    ```json
    {
        "name": "string",
        "sku": "string",
        "price": "numeric",
        "stock": "int",
        "supplier_id": "long",
        "category_id": "long",
        "is_active": "boolean"
    }
    ```
* **Query parameters:**
EMPTY
* **Path variable:**
EMPTY
* **Request response:**
    * **201 Created**
    ```json
    {
        "status": 201,
        "message": "Created: Product created successfully",
        "data":{
            "name": "string",
            "sku": "string",
            "price": "numeric",
            "stock": "int",
            "supplier":{
                "name": "string",
                "email": "string",
                "phone_number": "string",
                "address": "string"
            },
            "category":{
                "name": "string",
                "description": "string"
            },
            "is_active": "boolean",
            "created_at": "timestamp"
        }
    }
    ```
    * **409 Conflict**
    ```json
    {
        "status": 409,
        "message": "Conflict: Product with provided `sku` already exist"
    }
    ```
    * **404 Not found supplier**
    ```json
    {
        "status": 404,
        "message": "Not found: Supplier with provided `supplier_id` not found."
    }
    ```
    * **404 Not found category**
    ```json
    {
        "status": 404,
        "message": "Not found: Category with provided `category_id` not found."
    }
    ```
* **Validaciones:**
    * **Request body:**
        - `name`: debe ser string, no puede ser nullo.
        - `sku`: debe ser string, no puede ser nullo, debe ser unico.
        - `price`: debe ser numerico, no puede ser nullo, debe ser positivo, debe seguir una estructura definida(por definir).
        - `stock`: debe ser numerico, no puede ser nullo, debe ser positivo.
        - `supplier_id`: debe ser numerico, no puede ser nullo, debe ser positivo.
        - `category_id`: debe ser numerico, no puede ser nullo, debe ser positivo.
        - `is_active`: debe ser tipo boleano, no puede ser nullo.
* **Funcionamiento:**
    - validaciones de dto
        - Si hay un problema responder con error `400 Bad Request` definido en `Estructuras de Respuesta Comunes`.
    1. Validar si el usuario autenticado tiene rol `ADMIN` o `SUPER_ADMIN`.
        - Si no, responder con error `403 Forbidden` definido en `Estrucuras de respuesta comunes`.
    2. Validar si producto no existe en base de datos utilizando el identificador `sku`
        - Si existe responde con error `409 Conflict` definido en `Request response`.
    3. Validar si los `supplier_id` existe en la base de datos.
        - Si no,  responder con error `404 Not Found supplier` definido en `Request response`.
    4. Validar si los `category_id` existe en la base de datos.
        - Si no,  responder con error `404 Not Found category` definido en `Request response`.
    5. Iniciar transaction db.
    6. Crear nuevo producto en base de datos.
    7. Crear registro en Audit_Log(action=CREATE, product_id=id, auditor_id=jwt_id, reason=header_reason_si_existe).
    8. Hacer commit de la transaccion.
    9. Responder con el nuevo producto creado con `201 Created` definido  en `Request response`.
#### `PUT /api/product/{product_id}`
* **Descripcion:** Endpoint para editar datos de un porducto especifico con el `product_id` proporcionado en path variables.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
"X-AUDIT-REASON": (Opcional) Razón para la actualización.
* **Request body:**
    ```json
    {
        "name": "string",
        "price": "numeric",
        "stock": "int",
        "supplier_id": "long",
        "category_id": "long"
    }
    ```
* **Query parameters:**
EMPTY
* **Path variable:**
    * **Product ID**
        - La variable `product_id` debe ser de tipo `long` para identificar al producto a editar en la base de datos.
* **Request response:**
    * **200 Successfull**
        ```json
        {
            "status": 200,
            "message": "Successfull: Product edited successfully",
            "data":{
                "name": "string",
                "sku": "string",
                "price": "numeric",
                "stock": "int",
                "supplier":{
                    "name": "string",
                    "email": "string",
                    "phone_number": "string",
                    "address": "string"
                },
                "category":{
                    "name": "string",
                    "description": "string"
                },
                "is_active": "boolean",
                "created_at": "timestamp"
            }
        }
        ```
    * **404 Not found**
        ```json
        {
            "status": 404,
            "message": "Not found: Product not found with provided `product_id`."
        }
        ```
    * **404 Not found supplier**
        ```json
        {
            "status": 404,
            "message": "Not found: Supplier not found with `supplier_id` provided."
        }
        ```
    * **404 Not found category**
        ```json
        {
            "status": 404,
            "message": "Not found: Category not found with `category_id` provided."
        }
        ```
* **Validaciones:**
    * **Request Body:**
        - **nota**: los valores `is_active` y `sku` no pueden ser editados en este endpoint.
        - `name`: no debe ser vacio
        - `price`: no debe ser vacio, debe ser numerico
        - `stock`: no debe ser vacio, debe ser numerico
        - `supplier_id`: no debe ser vacio, debe ser numerico positivo
        - `category_id`: no debe ser vacio, debe ser numerico positivo
* **Funcionamiento:**
    - validaciones de dto
        - Si hay un problema responder con error `400 Bad Request` definido en `Estructuras de Respuesta Comunes`.
    1. Validar que usuario autenticado sea `ADMIN` o `SUPER_ADMIN`
        - Si no, responder con error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
    2. Consultar en la base de datos si el producto a editar existe.
        - Si no, responder con error `404 Not found` definido en `Request response`.
    3. Validar si el `supplier_id` existe en la base de datos.
        - Si no, responder con error `404 Not found supplier` definido en `Request response`.
    4. Validar si el `category_id` existe en la base de datos.
        - Si no, responder con error `404 Not found category` definido en `Request response`.
    5. Iniciar transaction db.
    6. Actualizar objeto obtenido de consulta anterior y guardarlo con los datos editados.
    7. Crear objeto de Audit_Log(action=UPDATE, product_id=id, auditor_id=jwt_id, reason=header_reason_si_existe).
    8. Hacer commit de transaction.
    9. Responder con mensaje `200 Successfull` definido en `Request response`.
#### `POST /api/product/{product_id}/activate`
* **Descripcion:** Endpoint especifico para activar productos, esta accion solo la puede realizar un `ADMIN` o `SUPER_ADMIN` por ahora.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
"X-AUDIT-REASON": (Opcional) razon de la activacion.
* **Request body:**
EMPTY
* **Query parameters:**
EMPTY
* **Path variable:**
    * **Product ID**
        - La variable `product_id` debe ser de tipo `long` para identificar al producto a editar en la base de datos.
* **Request response:**
    * **200 Successfull**
    ```json
    {
        "status": 200,
        "message": "Successfull: Product activated successfully."
    }
    ```
    * **404 Not found**
    ```json
    {
        "status": 404,
        "message": "Not found: Product not found with `product_id` provided."
    }
    ```
* **Validaciones:**
EMPTY
* **Funcionamiento:**
    1. Validar que el usuario autenticado tenga rol `ADMIN` o `SUPER_ADMIN`
        - Si no, responder con error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
    2. Validar que el producto exista en la base de datos con el `product_id`.
        Si no, responder con error `404 Not found` definido en `Request response`.
    3. Validar que el producto no este `activo` con `is_active=true`
        - Si si esta, responder con `200 Successfull` definido en `Request response`.
    4. Iniciar transaction db.
    5. Editar el valor de `is_active` a `true` en el objeto y guardarlo.
    6. Crear objeto de Audit_Log(action=UPDATE, product_id=id, auditor_id=jwt_id, reason=header_reason_si_existe/default=Product activated).
    7. Hacer commit de la transaccion.
    8. Responder con `200 Successfull` definido en `Request response`.
#### `POST /api/product/{product_id}/deactivate`
* **Descripcion:** Endpoint especifico para desactivar productos, esta accion solo la puede realizar un `ADMIN` o `SUPER_ADMIN` por ahora.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
"X-AUDIT-REASON": (Opcional) razon de la desactivacion.
* **Request body:**
EMPTY
* **Query parameters:**
EMPTY
* **Path variable:**
    * **Product ID**
        - La variable `product_id` debe ser de tipo `long` para identificar al producto a editar en la base de datos.
* **Request response:**
    * **200 Successfull**
    ```json
    {
        "status": 200,
        "message": "Successfull: Product deactivated successfully."
    }
    ```
    * **404 Not found**
    ```json
    {
        "status": 404,
        "message": "Not found: Product not found with `product_id` provided."
    }
    ```
* **Validaciones:**
EMPTY
* **Funcionamiento:**
    1. Validar que el usuario autenticado tenga rol `ADMIN` o `SUPER_ADMIN`
        - Si no, responder con error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
    2. Validar que el producto exista en la base de datos con el `product_id`.
        Si no, responder con error `404 Not found` definido en `Request response`.
    3. Validar que el producto no este `desactivado` con `is_active=false`
        - Si si esta, responder con `200 Successfull` definido en `Request response`.
    4. Iniciar transaction db.
    5. Editar el valor de `is_active` a `false` en el objeto y guardarlo.
    6. Crear objeto de Audit_Log(action=UPDATE, product_id=id, auditor_id=jwt_id, reason=header_reason_si_existe/default=Product deactivated).
    7. Hacer commit de la transaccion.
    8. Responder con `200 Successfull` definido en `Request response`.
#### `DELETE /api/product/{product_id}`
* **Descripcion:** Endpoint para eliminar un producto en especifico con el `product_id` proporcionado, esta accion solo la puede hacer un `ADMIN` o `SUPER_ADMIN`
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
"X-AUDIT-REASON": Obligatorio razon de la eliminacion.
* **Request body:**
EMPTY
* **Query parameters:**
EMPTY
* **Path variable:**
    * **Product ID**
        - La variable `product_id` debe ser de tipo `long` para identificar al producto a eliminar en la base de datos.
* **Request response:**
    * **200 Successfull**
    ```json
    {
        "status": 200,
        "message": "200 Successfull: Product deleted with provided `product_id`."
    }
    ```
    * **404 Not found**
    ```json
    {
        "status": 404,
        "message": "404 Not found: Product not found with provided `product_id`."
    }
    ```
* **Validaciones:**
EMPTY
* **Funcionamiento:**
    1. Validar que usuario autenticado tenga rol `ADMIN` o `SUPER_ADMIN`
        - Si no, responder con error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
    2. Validar que producto exista y que no este "eliminado" con `deleted_at!=null` en la base de datos.
        - Si no, responder con error `404 Not found` definido en `Request response`.
    3. Validar que la razon este incluida y que sea menor de 100 caracteres.
        - Si no, responder con error `400 Bad Request` definido en `Estructuras de Respuesta Comunes`.
            - si no existe: `[ { "field": "X-Audit-Reason", "issue": "Header is required for delete operations" } ]`.
            - si excede la longitud: `[ { "field": "X-Audit-Reason", "issue": "Reason must be 100 characters or less" } ]`.
    4. Iniciar transaction db.
    5. "Eliminar" producto de base de datos.
    6. Crear objeto de Audit_Log(action=DELETE, product_id=id, auditor_id=jwt_id, reason=header_reason).
    7. Hacer commit de transaction.
    8. Responder con mensaje `200 Successfull` definido en `Request response`.
#### `POST /api/product/{product_id}/stock`
* **Descripcion:** Endpoint para editar el stock de un producto especifico mediante `product_id`, esta accion debe ocurrir en casos excepcionales donde se requeria modificar el stock de un producto sin generar una venta, este servicio solo puede ser usado por usuarios con rol `ADMIN` o `SUPER_ADMIN` por ahora.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
* **Request body:**
    ```json
    {
        "adjustment": "int",
        "reason": "string"
    }
    ```
* **Query parameters:**
EMPTY
* **Path variable:**
    * **Product ID**
        - La variable `product_id` debe ser de tipo `long` para identificar al producto a editar en la base de datos.
* **Request response:**
    * **200 Successfull**
    ```json
    {
        "status": 200,
        "message": "Successfull: Stock updated for `product_id` provided successfully."
    }
    ```
    * **404 Not found**
    ```json
    {
        "status": 404,
        "message": "Not found: Product not found with `product_id` provided."
    }
    ```
* **Validaciones:**
    * **Body**
        - `adjustment`: el valor a modificar el stock original, puede ser positivo o negativo
        - `reason`: debe ser tipo string, no debe ser vacio
* **Funcionamiento:**
    1. Validar que el usuario autenticado tenga rol `ADMIN` o `SUPER_ADMIN`.
        - Si no, responder con error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
    2. Validar que producto "exista" en la base de datos mediante el `product_id` proporcionado.
        - Si no, responder con error `404 Not found` definido en `Request response`.
    3. Crear transaction a db.
    4. Crear nuevo objeto de entidad `Stock_Movement`.
        - `product_id`: id del producto a editar proporcionado mediante los `Path variable`.
        - `user_id`: id del usuario autenticado que esta realizando la accion.
        - `adjustment`: cantidad a ajustar, puede ser negativo o positivo.
        - `reason`: razon por la edicion(ej. nuevo inventario).
    5. Ajustar el valor de stock en el objeto de producto y guardar en base de datos.
    6. Hacer commit de la transaction.
    7. Responder con mensaje `200 Successfull` definido en `Request response`. 

#### **SUPPLIER**
#### `GET /api/supplier`
* **Descripcion:** Endpoint para listar los suppliers guardados en la base de datos.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
* **Request body:**
EMPTY
* **Query parameters:**
    * **Pagination:**
        - `page`: Número de la página de resultados que se desea recuperar. El valor predeterminado suele ser 0 (primera página).
        - `size`: Cantidad de elementos a incluir en cada página. El valor predeterminado suele ser 10 o 20.
    * **Filters:**
        - `search`: Filtra por `name`, debe ser un valor tipo `string` para ser buscado en la base de datos.
    * **Sorts:**
        - `sort=name`: debe tener un valor `asc` o `desc` para el correcto ordenado, haciendo un ordenamiento de nombre.
        - `sort=created_at`: debe tener un valor `asc` o `desc` para el correcto ordenado por el valor de `timestamp` para saber cual se creo primero o el ultimo.
* **Path variable:**
EMPTY
* **Request response:**
    * **200 Successfull**
    ```json
    {
        "status": 200,
        "message": "Successfull",
        "data": {
            "content": [
                {
                    "name": "string",
                    "email": "string",
                    "phone_number": "string",
                    "address": "string"
                }
            ],
            "pagination": {
                "page": 0,           // Número de página actual
                "size": 10,          // Tamaño de página solicitado
                "totalElements": 50, // Total de elementos en todas las páginas
                "totalPages": 5,     // Total de páginas disponibles
                "last": false        // Indica si es la última página
            }
        }
    }
    ```
* **Validaciones:**
    * **Pagination:**
        - `page`: debe ser un numero entero positivo.
        - `size`: debe ser un numero entero positivo.   
    * **Filters:**
        - `search`: debe ser un valor string, debe existir cuando se usa el servicio con la intencion de obtener los suppliers que tienen un nombre unico.
    * **Sorts:**
        - `sort=name`: debe tener un valor `asc` o `desc` para ordenamiento.
        - `sort=created_at`: debe tener un valor `asc` o `desc` para ordenamiento.
* **Funcionamiento:**
    - Validaciones de sorts y filters
        - Si hay algun problema con filtros o sorts, responder con error `400 Bad Request` definido en `Estructuras de Respuesta Comunes`
    1. Si hay filtros o sorts, hacer las validaciones y acciones correctas para incluirlas en la consulta a la base de datos.
    2. Leer query parameters, o establecer valores por defecto `page=0` y `size=10`.
    3. Hacer consulta a base de datos con el conteo total de suppliers para mandarlo en la respuesta.
    4. Hacer consulta a la base de datos con la query creada con o sin filtros.
    5. Responder con mensaje `200 Successfull` definido en `Request response`.
#### `GET /api/supplier/{supplier_id}`
* **Descripcion:** Endpoint para listar los datos de un supplier en especifico a travez del path variable `supplier_id`.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
* **Request body:**
EMPTY
* **Query parameters:**
EMPTY
* **Path variable:**
    * **Supplier ID:** El parametro debe ser numero entero y positivo.
* **Request response:**
    * **200 Successfull**
    ```json
    {
        "status": 200,
        "message": "Successfull",
        "data": {
            "name": "string",
            "email": "string",
            "phone_number": "string",
            "address": "string"
        }
    }
    ```
    * **404 Not found**
    ```json
    {
        "status": 404,
        "message": "Not found: Supplier not found with `supplier_id` provided`."
    }
    ```
* **Validaciones:**
    * **Path variable:**
        - `supplier_id`: debe ser numerico entero y positivo.
* **Funcionamiento:**
    - Validaciones de path variable `supplier_id`
        - Si hay algun problema con el `supplier_id`, responder con error `400 Bad Request` definido en `Estructuras de Respuesta Comunes`.
    1. Validar si el supplier existe en la base de datos con el `supplier_id` proporcionado, esta validacion sera si exsite en la base de datos o si esta "eliminado".
        - Si no, Responder con mensaje de error `404 Not found` definido en `Request response`.
    2. Responder con mensaje `200 Successfull` definido en `Request response`.
#### `POST /api/supplier`
* **Descripcion:** Endpoint para anadir un supplier nuevo a la base de datos.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
"X-AUDIT-REASON": (Opcional) Razón para la creación.
* **Request body:**
    ```json
    {
        "name": "string",
        "email": "string",
        "phone_number": "string",
        "address": "string"
    }
    ```
* **Query parameters:**
EMPTY
* **Path variable:**
EMPTY
* **Request response:**
    * **201 Created**
    ```json
    {
        "status": 201,
        "message": "Created: Supplier created successfully.",
        "data": {
            "name": "string",
            "email": "string",
            "phone_number": "string",
            "address": "string"
        }
    }
    ```
    * **409 Conflict**
    ```json
    {
        "status": 409,
        "message": "Conflict: Supplier with provided `name` already exists."
    }
    ```
* **Validaciones:**
    * **Request body**
        - `name`: debe ser string, no puede ser nullo, debe ser unico.
        - `email`: debe ser string, no puede ser nullo, debe ser email.
        - `phone_number`: debe ser string, debe tener una longitud minima de 7 caracteres, debe tener un a longitud maxima de 20 caracteres, debe seguir este formato `^[0-9+\-() ]*$`, solo puede tener estos caracteres `+, -, ( y )`.
        - `address`: debe ser string
* **Funcionamiento:**
    - validaciones de dto
        - Si hay un problema responder con error `400 Bad Request` definido en `Estructuras de Respuesta Comunes`.
    1. Validar si el usuario autenticado es `ADMIN` o `SUPER_ADMIN`.
        - Si no, responder con error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
    2. Validar si supplier no existe en base de datos utilizando el identificador `name`
        - Si existe responde con error `409 Conflict` definido en `Request response`.
    3. Iniciar transaction db.
    4. Crear nuevo supplier en la base de datos.
    5. Crear registro en Audit_Log(action=CREATE, supplier_id=id, auditor_id=jwt_id, reason=header_reason_si_existe).
    6. Hacer commit de la transaccion.
    7. Responder con mensaje `201 Created` definido en `Request response`.
#### `PUT /api/supplier/{supplier_id}`
* **Descripcion:** Endpoint para editar datos de un supplier especifico con el `supplier_id` proporcionado en path variables.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
"X-AUDIT-REASON": (Opcional) Razón para la actualización.
* **Request body:**
    ```json
    {
        "name": "string",
        "email": "string",
        "phone_number": "string",
        "address": "string"
    }
    ```
* **Query parameters:**
EMPTY
* **Path variable:**
    * **Supplier ID**
        - La variable `supplier_id` debe ser de tipo `long` para identificar al supplier a editar en la base de datos.
* **Request response:**
    * **200 Successfull**
    ```json
    {
        "status": 200,
        "message": "Succesfull: Supplier edited successfully.",
        "data": {
            "name": "string",
            "email": "string",
            "phone_number": "string",
            "address": "string"
        }
    }
    ```
    * **409 Conflict**
    ```json
    {
        "status": 409,
        "message": "Conflict: Supplier with provided `name` already exists."
    }
    ```
    * **404 Not found**
    ```json
    {
        "status": 404,
        "message": "Not found: Supplier not found with provided `supplier_id`."
    }
    ```
* **Validaciones:**
    * **Request body**
        - `name`: debe ser string, no puede ser nullo, debe ser unico.
        - `email`: debe ser string, no puede ser nullo, debe ser email.
        - `phone_number`: debe ser string, debe tener una longitud minima de 7 caracteres, debe tener un a longitud maxima de 20 caracteres, debe seguir este formato `^[0-9+\-() ]*$`, solo puede tener estos caracteres `+, -, ( y )`.
        - `address`: debe ser string
* **Funcionamiento:**
    - validaciones de dto
        - Si hay un problema responder con error `400 Bad Request` definido en `Estructuras de Respuesta Comunes`.
    1. Validar que usuario autenticado sea `ADMIN` o `SUPER_ADMIN`
        - Si no, responder con error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
    2. Consultar en la base de datos si el supplier a editar existe.
        - Si no, responder con error `404 Not found` definido en `Request response`.
    3. Validar si el nuevo `name` especificado en `Request body` existe en la base de datos (`SELECT * FROM supplier WHERE name = ? AND id != ?`).
        - Si si existe, responder con error `409 Conflict` definido en `Request response`.
    4. Iniciar transaction db.
    5. Actualizar objeto obtenido de consulta anterior y guardarlo con los datos editados.
    6. Crear objeto de Audit_Log(action=UPDATE, supplier_id=id, auditor_id=jwt_id, reason=header_reason_si_existe).
    7. Hacer commit de transaction.
    8. Responder con mensaje `200 Successfull` definido en `Request response`.
#### `DELETE /api/supplier/{supplier_id}`
* **Descripcion:** Endpoint para eliminar un supplier en especifico con el `supplier_id` proporcionado, esta accion solo la puede hacer un `ADMIN` o `SUPER_ADMIN`
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
"X-AUDIT-REASON": Obligatorio razon de la eliminacion.
* **Request body:**
EMPTY
* **Query parameters:**
EMPTY
* **Path variable:**
    * **Supplier ID**
        - La variable `supplier_id` debe ser de tipo `long` para identificar al supplier a eliminar en la base de datos.
* **Request response:**
    * **200 Successfull**
    ```json
    {
        "status": 200,
        "message": "200 Successfull: Supplier deleted with provided `supplier_id`."
    }
    ```
    * **404 Not found**
    ```json
    {
        "status": 404,
        "message": "404 Not found: Supplier not found with provided `supplier_id`."
    }
    ```
* **Validaciones:**
EMPTY
* **Funcionamiento:**
    1. Validar que usuario autenticado tenga rol `ADMIN` o `SUPER_ADMIN`
        - Si no, responder con error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
    2. Validar que supplier exista y que no este "eliminado" con `deleted_at!=null` en la base de datos.
        - Si no, responder con error `404 Not found` definido en `Request response`.
    3. Validar que la razon este incluida y que sea menor de 100 caracteres.
    - Si no, responder con error `400 Bad Request` definido en `Estructuras de Respuesta Comunes`.
        - si no existe: `[ { "field": "X-Audit-Reason", "issue": "Header is required for delete operations" } ]`.
        - si excede la longitud: `[ { "field": "X-Audit-Reason", "issue": "Reason must be 100 characters or less" } ]`.
    4. Iniciar transaction db.
    5. "Eliminar" supplier de base de datos.
    6. Crear objeto de Audit_Log(action=DELETE, supplier_id=id, auditor_id=jwt_id, reason=header_reason).
    7. Hacer commit de transaction.
    8. responder con mensaje `200 Successfull` definido en `Request response`.

#### **CATEGORY**
#### `GET /api/category`
* **Descripcion:** Endpoint para listar los category guardados en la base de datos.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
* **Request body:**
EMPTY
* **Query parameters:**
    * **Pagination:**
        - `page`: Número de la página de resultados que se desea recuperar. El valor predeterminado suele ser 0 (primera página).
        - `size`: Cantidad de elementos a incluir en cada página. El valor predeterminado suele ser 10 o 20.
    * **Filters:**
        - `search`: Filtra por `name`, debe ser un valor tipo `string` para ser buscado en la base de datos.
    * **Sorts:**
        - `sort=name`: debe tener un valor `asc` o `desc` para el correcto ordenado, haciendo un ordenamiento de nombre.
        - `sort=created_at`: debe tener un valor `asc` o `desc` para el correcto ordenado por el valor de `timestamp` para saber cual se creo primero o el ultimo.
* **Path variable:**
EMPTY
* **Request response:**
    * **200 Successfull**
    ```json
    {
        "status": 200,
        "message": "Successfull",
        "data": {
            "content": [
                {
                    "name": "string",
                    "description": "string",
                }
            ],
            "pagination": {
                "page": 0,           // Número de página actual
                "size": 10,          // Tamaño de página solicitado
                "totalElements": 50, // Total de elementos en todas las páginas
                "totalPages": 5,     // Total de páginas disponibles
                "last": false        // Indica si es la última página
            }
        }
    }
    ```
* **Validaciones:**
    * **Pagination:**
        - `page`: debe ser un numero entero positivo.
        - `size`: debe ser un numero entero positivo.   
    * **Filters:**
        - `search`: debe ser un valor string, debe existir cuando se usa el servicio con la intencion de obtener los category que tienen un nombre unico.
    * **Sorts:**
        - `sort=name`: debe tener un valor `asc` o `desc` para ordenamiento.
        - `sort=created_at`: debe tener un valor `asc` o `desc` para ordenamiento.
* **Funcionamiento:**
    - Validaciones de sorts y filters
        - Si hay algun problema con filtros o sorts, responder con error `400 Bad Request` definido en `Estructuras de Respuesta Comunes`
    1. Si hay filtros o sorts, hacer las validaciones y acciones correctas para incluirlas en la consulta a la base de datos.
    2. Leer query parameters, o establecer valores por defecto `page=0` y `size=10`.
    3. Hacer consulta a base de datos con el conteo total de category para mandarlo en la respuesta.
    4. Hacer consulta a la base de datos con la query creada con o sin filtros.
    5. Responder con mensaje `200 Successfull` definido en `Request response`.
#### `GET /api/category/{category_id}`
* **Descripcion:** Endpoint para listar los datos de un category en especifico a travez del path variable `category_id`.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
* **Request body:**
EMPTY
* **Query parameters:**
EMPTY
* **Path variable:**
    * **Category ID:** El parametro debe ser numero entero y positivo.
* **Request response:**
    * **200 Successfull**
    ```json
    {
        "status": 200,
        "message": "Successfull",
        "data": {
            "name": "string",
            "description": "string",
        }
    }
    ```
    * **404 Not found**
    ```json
    {
        "status": 404,
        "message": "Not found: Category not found with `category_id` provided`."
    }
    ```
* **Validaciones:**
    * **Path variable:**
        - `category_id`: debe ser numerico entero y positivo.
* **Funcionamiento:**
    - Validaciones de path variable `category_id`
        - Si hay algun problema con el `category_id`, responder con error `400 Bad Request` definido en `Estructuras de Respuesta Comunes`.
    1. Validar si el category existe en la base de datos con el `category_id` proporcionado, esta validacion sera si exsite en la base de datos o si esta "eliminado".
        - Si no, Responder con mensaje de error `404 Not found` definido en `Request response`.
    2. Responder con mensaje `200 Successfull` definido en `Request response`.
#### `POST /api/category`
* **Descripcion:** Endpoint para anadir un category nuevo a la base de datos.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
"X-AUDIT-REASON": (Opcional) Razón para la creación.
* **Request body:**
    ```json
    {
        "name": "string",
        "description": "string",
    }
    ```
* **Query parameters:**
EMPTY
* **Path variable:**
EMPTY
* **Request response:**
    * **201 Created**
    ```json
    {
        "status": 201,
        "message": "Created: Category created successfully.",
        "data": {
            "name": "string",
            "description": "string",
        }
    }
    ```
    * **409 Conflict**
    ```json
    {
        "status": 409,
        "message": "Conflict: Category with provided `name` already exists."
    }
    ```
* **Validaciones:**
    * **Request body**
        - `name`: debe ser string, no puede ser nullo, debe ser unico.
        - `description`: debe ser string, no puede ser nullo.
* **Funcionamiento:**
    - validaciones de dto
        - Si hay un problema responder con error `400 Bad Request` definido en `Estructuras de Respuesta Comunes`.
    1. Validar si el usuario autenticado es `ADMIN` o `SUPER_ADMIN`.
        - Si no, responder con error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
    2. Validar si category no existe en base de datos utilizando el identificador `name`
        - Si existe responde con error `409 Conflict` definido en `Request response`.
    3. Iniciar transaction db.
    4. Crear nueva category.
    5. Crear registro en Audit_Log(action=CREATE, category_id=id, auditor_id=jwt_id, reason=header_reason_si_existe).
    6. Hacer commit de la transaction.
    7. Responder con mensaje `201 Created` definido en `Request response`.
#### `PUT /api/category/{category_id}`
* **Descripcion:** Endpoint para editar datos de un category especifico con el `category_id` proporcionado en path variables.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
"X-AUDIT-REASON": (Opcional) Razón para la actualización.
* **Request body:**
    ```json
    {
        "name": "string",
        "description": "string",
    }
    ```
* **Query parameters:**
EMPTY
* **Path variable:**
    * **Category ID**
        - La variable `category_id` debe ser de tipo `long` para identificar al category a editar en la base de datos.
* **Request response:**
    * **200 Successfull**
    ```json
    {
        "status": 200,
        "message": "Succesfull: Category edited successfully.",
        "data": {
            "name": "string",
            "description": "string",
        }
    }
    ```
    * **409 Conflict**
    ```json
    {
        "status": 409,
        "message": "Conflict: Category with provided `name` already exists."
    }
    ```
    * **404 Not found**
    ```json
    {
        "status": 404,
        "message": "Not found: Category not found with provided `category_id`."
    }
    ```
* **Validaciones:**
    * **Request body**
        - `name`: debe ser string, no puede ser nullo, debe ser unico.
        - `description`: debe ser string, no puede ser nullo.
* **Funcionamiento:**
    - validaciones de dto
        - Si hay un problema responder con error `400 Bad Request` definido en `Estructuras de Respuesta Comunes`.
    1. Validar que usuario autenticado sea `ADMIN` o `SUPER_ADMIN`
        - Si no, responder con error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
    2. Consultar en la base de datos si el category a editar existe.
        - Si no, responder con error `404 Not found` definido en `Request response`.
    3. Validar si el nuevo `name` especificado en `Request body` existe en la base de datos (`SELECT * FROM category WHERE name = ? AND id != ?`).
        - Si si existe, responder con error `409 Conflict` definido en `Request response`.
    4. Iniciar transaction db.
    5. Actualizar objeto obtenido de consulta anterior y guardarlo con los datos editados.
    6. Crear objeto de Audit_Log(action=UPDATE, category_id=id, auditor_id=jwt_id, reason=header_reason_si_existe).
    7. Hacer commit de transaction.
    8. Responder con mensaje `200 Successfull` definido en `Request response`.
#### `DELETE /api/category/{category_id}`
* **Descripcion:** Endpoint para eliminar un category en especifico con el `category_id` proporcionado, esta accion solo la puede hacer un `ADMIN` o `SUPER_ADMIN`
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
"X-AUDIT-REASON": Obligatorio razon de la eliminacion.
* **Request body:**
EMPTY
* **Query parameters:**
EMPTY
* **Path variable:**
    * **Category ID**
        - La variable `category_id` debe ser de tipo `long` para identificar al category a eliminar en la base de datos.
* **Request response:**
    * **200 Successfull**
    ```json
    {
        "status": 200,
        "message": "200 Successfull: Category deleted with provided `category_id`."
    }
    ```
    * **404 Not found**
    ```json
    {
        "status": 404,
        "message": "404 Not found: Category not found with provided `category_id`."
    }
    ```
    * **409 Conflict**
    ```json
    {
        "status": 409,
        "message": "Conflict: Category is still assigned to one or more products."
    }
    ```
* **Validaciones:**
EMPTY
* **Funcionamiento:**
    1. Validar que usuario autenticado tenga rol `ADMIN` o `SUPER_ADMIN`
        - Si no, responder con error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
    2. Validar que category exista y que no este "eliminado" con `deleted_at!=null` en la base de datos.
        - Si no, responder con error `404 Not found` definido en `Request response`.
    3. Validar si category no esta asignado en productos.
        - Si si esta, responder con error `409 Conflict` definido en `Request response`.
    4. Validar que la razon este incluida y que sea menor de 100 caracteres.
        - Si no, responder con error `400 Bad Request` definido en `Estructuras de Respuesta Comunes`.
            - si no existe: `[ { "field": "X-Audit-Reason", "issue": "Header is required for delete operations" } ]`.
            - si excede la longitud: `[ { "field": "X-Audit-Reason", "issue": "Reason must be 100 characters or less" } ]`.
    5. Iniciar transaction db.
    6. "Eliminar" category de base de datos.
    7. Crear objeto de Audit_Log(action=DELETE, category_id=id, auditor_id=jwt_id, reason=header_reason).
    8. Hacer commit de transaction.
    9. responder con mensaje `200 Successfull` definido en `Request response`.

#### Modulo de Transacciones y Ventas
#### `POST /api/sale`
* **Descripcion:** Endpoint para crear una venta en la base de datos.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
* **Request body:**
    ```json
    {
        "notes": "string",
        "payment_method": "ENUM(CASH, CARD, TRANSFER)",
        "products":[
            {
                "product_id": "long",
                "quantity": "int",
            }
        ]
    }
    ```
* **Query parameters:**
EMPTY
* **Path variable:**
EMPTY
* **Request response:**
    * **201 Created**
    ```json
    {
        "status": 201,
        "message": "Created: Sale created successfully.",
        "data": {
            "sale_id": "long",
            "total": "numeric",
            "status": "string",
            "payment_method": "string",
            "created_at": "timestamp",
            "details": [
                {
                    "id": "long",
                    "product": {
                        "name": "string",
                        "SKU": "string",
                        "price": "numeric",
                        "stock": "int",
                        "supplier":{
                            "name": "string",
                            "email": "string",
                            "phone_number": "string",
                            "address": "string"
                        },
                        "category":{
                            "name": "string",
                            "description": "string"
                        },
                        "is_active": "boolean",
                        "created_at": "timestamp"
                    },
                    "quantity": "int",
                    "notes": "string",
                    "created_at": "timestamp"
                }
            ]
        }
    }
    ```
    * **404 Not found (product)**
    ```json
    {
        "status": 404,
        "message": "Not found: Product listed not found, check list to know which.",
        "data": {
            "product_id": "long"
        }
        
    }
    ```
    * **409 Conflict (product)**
    ```json
    {
        "status": 409,
        "message": "Conflict: Product stock is not enough for the sale, check list to know which one.",
        "data": {
            "product_id": "long"
        }
    }
    ```
* **Validaciones:**
    * **Body**
        - `notes`: debe ser string, opcional.
        - `payment_method`: debe ser string, debe ser un valor de ENUM definido, no puede ser nullo.
        - `product`: debe ser una lista de productos a vender, debe incluir unicamente `product_id` y `quantity`
            - `product_id`: debe ser tipo entero y positivo, no puede ser nullo.
            - `quantity`: debe ser tipo entero y positivo, no puede ser nullo.
* **Funcionamiento:**
    - validaciones de dto
        - Si hay un problema responder con error `400 Bad Request` definido en `Estructuras de Respuesta Comunes`.
    1. Validar que el usuario autenticado tenga rol `CASHIER`, `ADMIN` o `SUPER_ADMIN`.
        - Si no, responder con error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
    2. Recorrer el listado de los productos para verificar si existen y hay stock suficiente.
        - Si no existe alguno, responder con error `404 Not found (product)` definido en `Request response`.
        - Si el stock no es suficiente, responder con error `409 Conflict` definido en `Request response`.
    3. Comenzar una transaccion de db para guardar los datos.
    4. Volver a recorrer el listado de productos para poder hacer los objetos de `Sale_Detail`.
    * 4.1. Mientras se hace el recorrido hacer la reduccion de el stock del producto.
    5. Hacer commit de la base de datos.
        - Este commit tendra los siguientes actualizaciones o creaciones:
            - Creacion de `Sale`.
            - Actualizacion de stock en productos afectados.
            - Creacion de `Sale_Details` de todos los productos.
    6. Responder con mensaje `201 Created` definido en `Request response`.
#### `GET /api/sale`
* **Descripcion:** Endpoint para listar las ventas, se pueden utilizar estos filtros y sorts `filter=cashier_id`, `filter=product_id`, `filter=category_id`, `filter=supplier_id`, `filter=date_from`, `filter=date_to`, `sort=created_at`, `sort=cashier`, `sort=total`.
    - Para el filtro `date_from`, si solo se incluye ese filtro se definira por default el `date_to` a la fecha actual.
    - Para el filtro `date_to`, si solo se incluye ese filtro se definira como default el `date_from` como la fecha inicial, osea la primera venta existente.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
* **Request body:**
EMPTY
* **Query parameters:**
    * **Pagination:**
        - `page`: Número de la página de resultados que se desea recuperar. El valor predeterminado suele ser 0 (primera página).
        - `size`: Cantidad de elementos a incluir en cada página. El valor predeterminado suele ser 10 o 20.
    * **Filters:**
        - `cashier_id`: Filtra por `cashier_id` en la entidad Sale.
        - `product_id`: Filtra por `product_id` en la entidad Sale_Detail.
        - `category_id`: Filtra por `category_id` en la entidad Sale_Detail este valor se obtiene desde la entidad Product mediante el `product_id`.
        - `supplier_id`: Filtra por `supplier_id` en la entidad Sale_Detail este valor se obtiene desde la entidad Product mediante el `product_id`.
        - `date_from`: Filtra los datos desde una fecha especifica.
            - Como default, este valor es el mismo al de la primera venta creada.
        - `date_to`: Filtra los datos hasta una fecha especifica.
            - Como default, este valor es el mismo al de la ultima venta creada.
    * **Sorts:**
        - `created_at`: debe tener un valor `asc` o `desc` para el correcto ordenado por el valor de `timestamp` para saber cual se creo primero o el ultimo.
        - `cashier`: debe tener un valor `asc` o `desc` para el correcto ordenado por el valor `cashier_id`.
        - `total`: debe tener un valor `asc` o `desc` para el correcto ordenado por total. 
* **Path variable:**
EMPTY
* **Request response:**
    * **200 Successfull**
    ```json
    {
        "status": 200,
        "message": "Successfull",
        "data": {
            "content": [
                {
                    "id": "long",
                    "cashier": {
                        "id": "long",
                        "name": "string",
                        "email": "string",
                        "created_at": "timestamp",
                        "role": {
                            "id": "long",
                            "name": "string",
                            "description": "string"
                        }
                    },
                    "total": "numeric",
                    "status": "string",
                    "payment_method": "string",
                    "created_at": "timestamp",
                    "details": [
                        {
                            "id": "long",
                            "sale_id": "long",
                            "product":{
                                "name": "string",
                                "SKU": "string",
                                "price": "numeric",
                                "stock": "int",
                                "supplier":{
                                    "name": "string",
                                    "email": "string",
                                    "phone_number": "string",
                                    "address": "string"
                                },
                                "category":{
                                    "name": "string",
                                    "description": "string"
                                },
                                "is_active": "boolean",
                                "created_at": "timestamp"
                            },
                            "quantity": "int",
                            "unit_price": "numeric",
                            "notes": "string",
                            "created_at": "timestamp"
                        }
                    ]
                }
            ],
            "pagination": {
                "page": 0,           // Número de página actual
                "size": 10,          // Tamaño de página solicitado
                "totalElements": 50, // Total de elementos en todas las páginas
                "totalPages": 5,     // Total de páginas disponibles
                "last": false        // Indica si es la última página
            }
        }
    }
    ```
* **Validaciones:**
    * **Pagination:**
        - `page`: debe ser un numero entero positivo.
        - `size`: debe ser un numero entero positivo.   
    * **Filters:**
        - `cashier_id`: debe ser un valor numerico no negativo, debe existir cuando se buscan las ventas de un unico cajero.
        - `product_id`: debe ser un valor numerico no negativo, debe existir cuando se buscan las ventas de un unico producto.
        - `category_id`: debe ser un valor numerico no negativo, debe existir cuando se buscan las ventas de una unica categoria.
        - `supplier_id`: debe ser un valor numerico no negativo, debe existir cuando se buscan las ventas de un unico supplier.
        - `date_from`: debe ser un valor string que se pueda convertir a tipo fecha.
        - `date_to`: debe ser un valor string que se pueda convertir a tipo fecha.
    * **Sorts:**
        - `cashier`: debe tener un valor `asc` o `desc` para ordenamiento.
        - `total`: debe tener un valor `asc` o `desc` para ordenamiento.
        - `created_at`: debe tener un valor `asc` o `desc` para ordenamiento.
* **Funcionamiento:**
    - Validaciones de sorts y filters
        - Si hay algun problema con filtros o sorts, responder con error `400 Bad Request` definido en `Estructuras de Respuesta Comunes`
    1. Validar si el usuario autenticado es `ADMIN` o `SUPER_ADMIN`, si no lo es asignar el id desde el jwt al filtro `cashier_id`.
    2. Si hay filtros o sorts, hacer las validaciones y acciones correctas para incluirlas en la consulta a la base de datos.
    2. Leer query parameters, o establecer valores por defecto `page=0` y `size=10`.
    3. Hacer consulta a base de datos con el conteo total de sales para mandarlo en la respuesta.
    4. Hacer consulta a la base de datos con la query creada con o sin filtros.
    5. Responder con mensaje `200 Successfull` definido en `Request response`.
#### `GET /api/sale/{sale_id}`
* **Descripcion:** Endpoint para listar los datos de un sale especifico a travez del path variable `sale_id`.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
* **Request body:**
EMPTY
* **Query parameters:**
EMPTY
* **Path variable:**
    * **Sale ID:** El valor debe ser numero entero y positivo.
* **Request response:**
    * **200 Successfull**
    ```json
    {
        "status": 200,
        "message": "Successfull.",
        "data":{
            "id": "long",
            "cashier": {
                "id": "long",
                "name": "string",
                "email": "string",
                "created_at": "timestamp",
                "role": {
                    "id": "long",
                    "name": "string",
                    "description": "string"
                }
            },
            "total": "numeric",
            "status": "string",
            "payment_method": "string",
            "created_at": "timestamp",
            "details": [
                {
                    "id": "long",
                    "sale_id": "long",
                    "product":{
                        "name": "string",
                        "SKU": "string",
                        "price": "numeric",
                        "stock": "int",
                        "supplier":{
                            "name": "string",
                            "email": "string",
                            "phone_number": "string",
                            "address": "string"
                        },
                        "category":{
                            "name": "string",
                            "description": "string"
                        },
                        "is_active": "boolean",
                        "created_at": "timestamp"
                    },
                    "quantity": "int",
                    "unit_price": "numeric",
                    "notes": "string",
                    "created_at": "timestamp"
                }
            ]
        }
    }
    ```
    * **404 Not found**
    ```json
    {
        "status": 404,
        "message": "Not found: Sale not found with provided `sale_id`."
    }
    ```
* **Validaciones:**
    * **Path variable:**
        - `sale_id`: debe ser numerico, entero y positivo.
* **Funcionamiento:**
    - Validacion de path variable `sale_id`.
        - Si hay algun problema con `sale_id`, responder con error `400 Bad Request` definido en `Estructuras de Respuesta Comunes`.
    1. Validar si el sale existe en la base de datos con el `sale_id` proporcionado.
        - Si no existe, responder con mensaje de error `404 Not found` definido en `Request response`.
    2. Validar si el usuario autenticado no es `ADMIN` o `SUPER_ADMIN`, si no es, validar si el id del usuario autenticado coincide con el de `cashier_id` del objeto obtenido de la base de datos.
        - Si no coincide, respondere con error `403 Forbidden` definido en `Estructuras de Respuesta Comunes`.
    3. Responder con mensaje `200 Successfull` definido en `Request response`.
#### `POST /api/sale/{sale_id}/cancel`
* **Descripcion:** Endpoint para cancelar una venta en especifico mediante el Path variable `sale_id`, este servicio solo puede ser usado por un `ADMIN` o `SUPER_ADMIN`.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
* **Request body:**
EMPTY
* **Query parameters:**
EMPTY
* **Path variable:**
    * **Sale ID:** El valor debe ser numero entero y positivo.
* **Request response:**
    * **200 Successfull**
    ```json
    {
        "status": 200,
        "message": "Successfull: Sale canceled successfully."
    }
    ```
    * **404 Not found**
    ```json
    {
        "status": 404,
        "message": "Not found: Sale not found with provide `sale_id`."
    }
    ```
    * **409 Conflict**
    ```json
    {
        "status": 409,
        "message": "Conflict: Only completed sales can be cancelled."
    }
    ```
* **Validaciones:**
    * **Path variable:**
        - `sale_id`: debe ser numerico, entero y positivo.
* **Funcionamiento:**
    - Validacion de path variable `sale_id`.
        - Si hay algun problema con `sale_id`, responder con error `400 Bad Request` definido en `Estructuras de Respuesta Comunes`.
    1. Validar si usuario autenticado tiene rol `ADMIN` o `SUPER_ADMIN`.
        - Si no, responder con error `403 Forbidden` definido  en `Estructuras de Respuesta Comunes`.
    2. Validar si existe en la base de datos utilizando el path variable `sale_id`.
        - Si no existe, responder con error `404 Not found` definido en `Estructuras de Respuesta Comunes`.
    3. Validar que el objeto tenga status = `COMPLETED`.
        - Si no es, enviar error `409 Conflict` definido en `Request response`.
    4. Comenzar transaction a db.
    5. Recorrer los Sale_Details de la venta, y sumar la cantidad al stock de cada producto de vuelta.
    6. Cambiar valor de status a `CANCELLED`
    7. Guardar en la base de datos los cambios.
    8. Commit db.
    9. Responder con `200 Successfull` definido en `Estructuras de Respuesta Comunes`.
#### `POST /api/sale/{sale_id}/total_refund`
* **Descripcion:** Endpoint para hacer un reembolso total de una venta en especifico mediante el Path variable `sale_id`, este servicio solo puede ser usado por un `ADMIN` o `SUPER_ADMIN`.
* **Headers:**
"Authorization" : Bearer {JWT_TOKEN}
* **Request body:**
EMPTY
* **Query parameters:**
EMPTY
* **Path variable:**
    * **Sale ID:** El valor debe ser numero entero y positivo.
* **Request response:**
    * **200 Successfull**
    ```json
    {
        "status": 200,
        "message": "Successfull: Sale refund successfully."
    }
    ```
    * **404 Not found**
    ```json
    {
        "status": 404,
        "message": "Not found: Sale not found with provide `sale_id`."
    }
    ```
    * **409 Conflict**
    ```json
    {
        "status": 409,
        "message": "Conflict: Only completed sales can be refunded."
    }
    ```
* **Validaciones:**
    * **Path variable:**
        - `sale_id`: debe ser numerico, entero y positivo.
* **Funcionamiento:**
    - Validacion de path variable `sale_id`.
        - Si hay algun problema con `sale_id`, responder con error `400 Bad Request` definido en `Estructuras de Respuesta Comunes`.
    1. Validar si usuario autenticado tiene rol `ADMIN` o `SUPER_ADMIN`.
        - Si no, responder con error `403 Forbidden` definido  en `Estructuras de Respuesta Comunes`.
    2. Validar si existe en la base de datos utilizando el path variable `sale_id`.
        - Si no existe, responder con error `404 Not found` definido en `Estructuras de Respuesta Comunes`.
    3. Validar que el objeto tenga status = `COMPLETED`.
        - Si no es, enviar error `409 Conflict` definido en `Request response`.
    4. Comenzar transaction a db.
    5. Recorrer los Sale_Details de la venta, y sumar la cantidad al stock de cada producto de vuelta.
    6. Cambiar valor de status a `TOTAL_REFUND`
    7. Guardar en la base de datos los cambios.
    8. Commit db.
    9. Responder con `200 Successfull` definido en `Estructuras de Respuesta Comunes`.
#### Manejo de errores
#### Notas
- Utilizar esta query de postgres para crear un unique parcial en sku para cuando se eliminan o crean productos
    - `CREATE UNIQUE INDEX unique_sku_active ON products (sku) WHERE deleted_at IS NULL;`
- Utilizar esta query de postgres para crear un unique parcial en name para cuando se eliminan o crean supplier
    - `CREATE UNIQUE INDEX unique_name_active ON supplier (name) WHERE deleted_at IS NULL;`
- Utilizar esta query de postgres para crear un unique parcial en name para cuando se eliminan o crean category
    - `CREATE UNIQUE INDEX unique_name_active ON category (name) WHERE deleted_at IS NULL;`
### Puntos importantes
- Si se usa un filtro cualquiera que sea busqueda unica responder con error 404
- Se implementará una entidad de auditoría `Audit_Log` para llevar un registro de las acciones comunes en la base de datos(CREACIÓN, ACTUALIZACIÓN, ELIMINACIÓN) centralizad para complementar el `SoftDelete`.

NUMERIC(12, 2) se traduce a BigDecimal en java
### Out of scope
* Crear una forma de que los roles puedan ser anadidos de forma dinamica, que cuando un rol sea creado poder asignar tareas de forma dinamica, esto quizas requerira modificaciones en la entidad de rol y en el codigo en general. Esto queda fuera del scope de momento
* Manejo de productos mediante batch
* Anadir mas estados a los productos (activo, inactivo) -> (activo, inactivo, agotado, descontinuado, pre_venta, reservado)
* Anadir mejor forma de search en los productos para mejorar el filtro
* Creacion de reportes de ventas y otros por definir.
* Anadir PARTIAL_REFUND a las ventas para hacer reembolso de un producto en especifico
