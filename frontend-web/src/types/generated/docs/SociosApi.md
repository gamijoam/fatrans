# SociosApi

All URIs are relative to *http://localhost:18080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**activarSocio**](SociosApi.md#activarsocio) | **PATCH** /api/v1/socios/{id}/activar | Activar un socio |
| [**actualizarSocio**](SociosApi.md#actualizarsocio) | **PUT** /api/v1/socios/{id} | Actualizar un socio |
| [**buscarSocios**](SociosApi.md#buscarsocios) | **GET** /api/v1/socios/buscar | Buscar socios por criterios |
| [**crearSocio**](SociosApi.md#crearsocio) | **POST** /api/v1/socios | Crear un nuevo socio |
| [**desactivarSocio**](SociosApi.md#desactivarsocio) | **PATCH** /api/v1/socios/{id}/desactivar | Desactivar un socio |
| [**eliminarSocio**](SociosApi.md#eliminarsocio) | **DELETE** /api/v1/socios/{id} | Eliminar un socio (soft delete) |
| [**listarSocios**](SociosApi.md#listarsocios) | **GET** /api/v1/socios | Listar socios con paginación |
| [**obtenerSocio**](SociosApi.md#obtenersocio) | **GET** /api/v1/socios/{id} | Obtener un socio por ID |



## activarSocio

> SocioResponseDTO activarSocio(id)

Activar un socio

### Example

```ts
import {
  Configuration,
  SociosApi,
} from '';
import type { ActivarSocioRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new SociosApi();

  const body = {
    // string
    id: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies ActivarSocioRequest;

  try {
    const data = await api.activarSocio(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | `string` |  | [Defaults to `undefined`] |

### Return type

[**SocioResponseDTO**](SocioResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## actualizarSocio

> SocioResponseDTO actualizarSocio(id, actualizarSocioDTO)

Actualizar un socio

### Example

```ts
import {
  Configuration,
  SociosApi,
} from '';
import type { ActualizarSocioRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new SociosApi();

  const body = {
    // string
    id: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
    // ActualizarSocioDTO
    actualizarSocioDTO: ...,
  } satisfies ActualizarSocioRequest;

  try {
    const data = await api.actualizarSocio(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | `string` |  | [Defaults to `undefined`] |
| **actualizarSocioDTO** | [ActualizarSocioDTO](ActualizarSocioDTO.md) |  | |

### Return type

[**SocioResponseDTO**](SocioResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## buscarSocios

> PageSocioResponseDTO buscarSocios(nombre, apellido, numeroDocumento, numeroSocio, correo, page, size)

Buscar socios por criterios

### Example

```ts
import {
  Configuration,
  SociosApi,
} from '';
import type { BuscarSociosRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new SociosApi();

  const body = {
    // string (optional)
    nombre: nombre_example,
    // string (optional)
    apellido: apellido_example,
    // string (optional)
    numeroDocumento: numeroDocumento_example,
    // string (optional)
    numeroSocio: numeroSocio_example,
    // string (optional)
    correo: correo_example,
    // number (optional)
    page: 56,
    // number (optional)
    size: 56,
  } satisfies BuscarSociosRequest;

  try {
    const data = await api.buscarSocios(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **nombre** | `string` |  | [Optional] [Defaults to `undefined`] |
| **apellido** | `string` |  | [Optional] [Defaults to `undefined`] |
| **numeroDocumento** | `string` |  | [Optional] [Defaults to `undefined`] |
| **numeroSocio** | `string` |  | [Optional] [Defaults to `undefined`] |
| **correo** | `string` |  | [Optional] [Defaults to `undefined`] |
| **page** | `number` |  | [Optional] [Defaults to `0`] |
| **size** | `number` |  | [Optional] [Defaults to `10`] |

### Return type

[**PageSocioResponseDTO**](PageSocioResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## crearSocio

> SocioResponseDTO crearSocio(crearSocioRequestDTO)

Crear un nuevo socio

### Example

```ts
import {
  Configuration,
  SociosApi,
} from '';
import type { CrearSocioRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new SociosApi();

  const body = {
    // CrearSocioRequestDTO
    crearSocioRequestDTO: ...,
  } satisfies CrearSocioRequest;

  try {
    const data = await api.crearSocio(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **crearSocioRequestDTO** | [CrearSocioRequestDTO](CrearSocioRequestDTO.md) |  | |

### Return type

[**SocioResponseDTO**](SocioResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## desactivarSocio

> SocioResponseDTO desactivarSocio(id, motivo)

Desactivar un socio

### Example

```ts
import {
  Configuration,
  SociosApi,
} from '';
import type { DesactivarSocioRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new SociosApi();

  const body = {
    // string
    id: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
    // string (optional)
    motivo: motivo_example,
  } satisfies DesactivarSocioRequest;

  try {
    const data = await api.desactivarSocio(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | `string` |  | [Defaults to `undefined`] |
| **motivo** | `string` |  | [Optional] [Defaults to `undefined`] |

### Return type

[**SocioResponseDTO**](SocioResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## eliminarSocio

> { [key: string]: string; } eliminarSocio(id, motivo)

Eliminar un socio (soft delete)

### Example

```ts
import {
  Configuration,
  SociosApi,
} from '';
import type { EliminarSocioRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new SociosApi();

  const body = {
    // string
    id: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
    // string (optional)
    motivo: motivo_example,
  } satisfies EliminarSocioRequest;

  try {
    const data = await api.eliminarSocio(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | `string` |  | [Defaults to `undefined`] |
| **motivo** | `string` |  | [Optional] [Defaults to `undefined`] |

### Return type

**{ [key: string]: string; }**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## listarSocios

> PageSocioResponseDTO listarSocios(page, size, sortBy, direction)

Listar socios con paginación

### Example

```ts
import {
  Configuration,
  SociosApi,
} from '';
import type { ListarSociosRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new SociosApi();

  const body = {
    // number (optional)
    page: 56,
    // number (optional)
    size: 56,
    // string (optional)
    sortBy: sortBy_example,
    // string (optional)
    direction: direction_example,
  } satisfies ListarSociosRequest;

  try {
    const data = await api.listarSocios(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **page** | `number` |  | [Optional] [Defaults to `0`] |
| **size** | `number` |  | [Optional] [Defaults to `10`] |
| **sortBy** | `string` |  | [Optional] [Defaults to `&#39;fechaRegistro&#39;`] |
| **direction** | `string` |  | [Optional] [Defaults to `&#39;desc&#39;`] |

### Return type

[**PageSocioResponseDTO**](PageSocioResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## obtenerSocio

> SocioResponseDTO obtenerSocio(id)

Obtener un socio por ID

### Example

```ts
import {
  Configuration,
  SociosApi,
} from '';
import type { ObtenerSocioRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new SociosApi();

  const body = {
    // string
    id: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies ObtenerSocioRequest;

  try {
    const data = await api.obtenerSocio(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | `string` |  | [Defaults to `undefined`] |

### Return type

[**SocioResponseDTO**](SocioResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

