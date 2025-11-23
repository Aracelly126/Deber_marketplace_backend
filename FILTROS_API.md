# API de Filtros para Productos - Sales System

## Resumen
Se ha implementado una funcionalidad avanzada de filtrado para la entidad `Producto` que permite realizar consultas sofisticadas con múltiples criterios de búsqueda.

## Endpoints Disponibles

### 1. Filtro Combinado (Más Potente)
**GET** `/api/products/filter`

**Descripción:** Permite combinar múltiples criterios de filtrado en una sola consulta.

**Parámetros (todos opcionales):**
- `minPrice` (Double): Precio mínimo
- `maxPrice` (Double): Precio máximo  
- `tipo` (String): Tipo de producto - valores: "PRODUCTO" o "SERVICIO"
- `searchTerm` (String): Término de búsqueda en nombre o descripción
- `ubicacion` (String): Ubicación del producto
- `disponibilidad` (Boolean): Si está disponible o no
- `page` (int): Número de página (por defecto 0)
- `size` (int): Tamaño de página (por defecto 20)
- `sort` (String): Campo para ordenar (ej: "precio,asc" o "nombre,desc")

**Ejemplo:**
```
GET /api/products/filter?minPrice=100&maxPrice=500&tipo=PRODUCTO&ubicacion=Madrid&page=0&size=10&sort=precio,asc
```

### 2. Filtro por Rango de Precio
**GET** `/api/products/filter/price`

**Descripción:** Filtra productos por un rango específico de precios.

**Parámetros:**
- `minPrice` (Double, requerido): Precio mínimo
- `maxPrice` (Double, requerido): Precio máximo
- Parámetros de paginación

**Ejemplo:**
```
GET /api/products/filter/price?minPrice=50&maxPrice=200&page=0&size=10
```

### 3. Filtro por Tipo de Producto
**GET** `/api/products/filter/type`

**Descripción:** Filtra productos por tipo (PRODUCTO o SERVICIO).

**Parámetros:**
- `tipo` (String, requerido): "PRODUCTO" o "SERVICIO"
- Parámetros de paginación

**Ejemplo:**
```
GET /api/products/filter/type?tipo=SERVICIO&page=0&size=10
```

### 4. Filtro por Ubicación
**GET** `/api/products/filter/location`

**Descripción:** Filtra productos por ubicación.

**Parámetros:**
- `ubicacion` (String, requerido): Ubicación a buscar
- Parámetros de paginación

**Ejemplo:**
```
GET /api/products/filter/location?ubicacion=Barcelona&page=0&size=10
```

### 5. Filtros de Precio Individual

#### Precio Mínimo
**GET** `/api/products/filter/min-price`

**Parámetros:**
- `minPrice` (Double, requerido): Precio mínimo

**Ejemplo:**
```
GET /api/products/filter/min-price?minPrice=100&page=0&size=10
```

#### Precio Máximo
**GET** `/api/products/filter/max-price`

**Parámetros:**
- `maxPrice` (Double, requerido): Precio máximo

**Ejemplo:**
```
GET /api/products/filter/max-price?maxPrice=500&page=0&size=10
```

### 6. Búsqueda con Paginación
**GET** `/api/products/search/paginated`

**Descripción:** Búsqueda de texto con soporte de paginación.

**Parámetros:**
- `searchTerm` (String, requerido): Término de búsqueda
- Parámetros de paginación

**Ejemplo:**
```
GET /api/products/search/paginated?searchTerm=laptop&page=0&size=10
```

## Estructura de Respuesta

Todos los endpoints de filtrado devuelven una respuesta paginada con la siguiente estructura:

```json
{
  "content": [
    {
      "id": 1,
      "nombre": "Producto Ejemplo",
      "descripcion": "Descripción del producto",
      "precio": 299.99,
      "tipo": "PRODUCTO",
      "estado": "ACTIVO",
      "ubicacion": "Madrid",
      "disponibilidad": true,
      "vendedorId": 123,
      "fechaCreacion": "2024-01-15T10:30:00"
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false
    },
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 150,
  "totalPages": 15,
  "last": false,
  "first": true,
  "numberOfElements": 10
}
```

## Casos de Uso Comunes

### 1. Buscar productos de tipo SERVICIO con precio entre 50-200€
```
GET /api/products/filter?tipo=SERVICIO&minPrice=50&maxPrice=200
```

### 2. Buscar productos disponibles en Madrid
```
GET /api/products/filter?ubicacion=Madrid&disponibilidad=true
```

### 3. Buscar productos caros (más de 1000€) ordenados por precio descendente
```
GET /api/products/filter/min-price?minPrice=1000&sort=precio,desc
```

### 4. Buscar servicios de consultoría en Barcelona
```
GET /api/products/filter?tipo=SERVICIO&searchTerm=consultoria&ubicacion=Barcelona
```

## Características Técnicas

- **Paginación:** Todos los endpoints soportan paginación con Spring Data
- **Ordenación:** Se puede ordenar por cualquier campo usando el parámetro `sort`
- **Validación:** Los tipos de producto se validan automáticamente
- **Rendimiento:** Las consultas utilizan JPQL optimizado
- **Flexibilidad:** Los filtros se pueden combinar libremente

## Notas Importantes

1. **Tipos de Producto:** Solo se aceptan "PRODUCTO" y "SERVICIO" (case-insensitive)
2. **Paginación por Defecto:** página 0, tamaño 20
3. **Búsqueda de Texto:** Busca en campos `nombre` y `descripcion` usando LIKE con comodines
4. **Filtros Opcionales:** En el endpoint combinado, todos los parámetros son opcionales
5. **Manejo de Errores:** Tipos inválidos devuelven HTTP 400 Bad Request





La funcionalidad de filtrado está **completamente implementada y operativa** en el puerto 8080.