# SolicitudesDeRegistroApi

All URIs are relative to *http://localhost:18080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**aprobarSolicitud**](SolicitudesDeRegistroApi.md#aprobarsolicitud) | **POST** /api/v1/socios/solicitudes/{id}/aprobar | Aprobar una solicitud de registro (Admin) |
| [**crearSolicitud**](SolicitudesDeRegistroApi.md#crearsolicitud) | **POST** /api/v1/socios/solicitud | Crear una nueva solicitud de registro |
| [**listarSolicitudes**](SolicitudesDeRegistroApi.md#listarsolicitudes) | **GET** /api/v1/socios/solicitudes | Listar solicitudes de registro (Admin) |
| [**rechazarSolicitud**](SolicitudesDeRegistroApi.md#rechazarsolicitud) | **POST** /api/v1/socios/solicitudes/{id}/rechazar | Rechazar una solicitud de registro (Admin) |



## aprobarSolicitud

> { [key: string]: object; } aprobarSolicitud(id, xAdminId, aprobarSolicitudRequestDTO)

Aprobar una solicitud de registro (Admin)

### Example

```ts
import {
  Configuration,
  SolicitudesDeRegistroApi,
} from '';
import type { AprobarSolicitudRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new SolicitudesDeRegistroApi();

  const body = {
    // string
    id: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
    // string
    xAdminId: xAdminId_example,
    // AprobarSolicitudRequestDTO (optional)
    aprobarSolicitudRequestDTO: ...,
  } satisfies AprobarSolicitudRequest;

  try {
    const data = await api.aprobarSolicitud(body);
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
| **xAdminId** | `string` |  | [Defaults to `undefined`] |
| **aprobarSolicitudRequestDTO** | [AprobarSolicitudRequestDTO](AprobarSolicitudRequestDTO.md) |  | [Optional] |

### Return type

**{ [key: string]: object; }**

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


## crearSolicitud

> { [key: string]: object; } crearSolicitud(solicitudRegistroRequestDTO)

Crear una nueva solicitud de registro

### Example

```ts
import {
  Configuration,
  SolicitudesDeRegistroApi,
} from '';
import type { CrearSolicitudRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new SolicitudesDeRegistroApi();

  const body = {
    // SolicitudRegistroRequestDTO
    solicitudRegistroRequestDTO: ...,
  } satisfies CrearSolicitudRequest;

  try {
    const data = await api.crearSolicitud(body);
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
| **solicitudRegistroRequestDTO** | [SolicitudRegistroRequestDTO](SolicitudRegistroRequestDTO.md) |  | |

### Return type

**{ [key: string]: object; }**

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


## listarSolicitudes

> { [key: string]: object; } listarSolicitudes(estado, page, size)

Listar solicitudes de registro (Admin)

### Example

```ts
import {
  Configuration,
  SolicitudesDeRegistroApi,
} from '';
import type { ListarSolicitudesRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new SolicitudesDeRegistroApi();

  const body = {
    // 'PENDIENTE' | 'APROBADA' | 'RECHAZADA' (optional)
    estado: estado_example,
    // number (optional)
    page: 56,
    // number (optional)
    size: 56,
  } satisfies ListarSolicitudesRequest;

  try {
    const data = await api.listarSolicitudes(body);
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
| **estado** | `PENDIENTE`, `APROBADA`, `RECHAZADA` |  | [Optional] [Defaults to `undefined`] [Enum: PENDIENTE, APROBADA, RECHAZADA] |
| **page** | `number` |  | [Optional] [Defaults to `0`] |
| **size** | `number` |  | [Optional] [Defaults to `10`] |

### Return type

**{ [key: string]: object; }**

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


## rechazarSolicitud

> { [key: string]: object; } rechazarSolicitud(id, xAdminId, rechazarSolicitudRequestDTO)

Rechazar una solicitud de registro (Admin)

### Example

```ts
import {
  Configuration,
  SolicitudesDeRegistroApi,
} from '';
import type { RechazarSolicitudRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new SolicitudesDeRegistroApi();

  const body = {
    // string
    id: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
    // string
    xAdminId: xAdminId_example,
    // RechazarSolicitudRequestDTO
    rechazarSolicitudRequestDTO: ...,
  } satisfies RechazarSolicitudRequest;

  try {
    const data = await api.rechazarSolicitud(body);
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
| **xAdminId** | `string` |  | [Defaults to `undefined`] |
| **rechazarSolicitudRequestDTO** | [RechazarSolicitudRequestDTO](RechazarSolicitudRequestDTO.md) |  | |

### Return type

**{ [key: string]: object; }**

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

