# BeneficiariosApi

All URIs are relative to *http://localhost:18080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**actualizarBeneficiario**](BeneficiariosApi.md#actualizarbeneficiario) | **PUT** /api/v1/socios/{socioId}/beneficiarios/{id} | Actualizar un beneficiario |
| [**crearBeneficiario**](BeneficiariosApi.md#crearbeneficiario) | **POST** /api/v1/socios/{socioId}/beneficiarios | Crear un nuevo beneficiario |
| [**eliminarBeneficiario**](BeneficiariosApi.md#eliminarbeneficiario) | **DELETE** /api/v1/socios/{socioId}/beneficiarios/{id} | Eliminar un beneficiario (soft delete) |
| [**listarBeneficiarios**](BeneficiariosApi.md#listarbeneficiarios) | **GET** /api/v1/socios/{socioId}/beneficiarios | Listar beneficiarios activos de un socio |
| [**obtenerBeneficiario**](BeneficiariosApi.md#obtenerbeneficiario) | **GET** /api/v1/socios/{socioId}/beneficiarios/{id} | Obtener un beneficiario por ID |



## actualizarBeneficiario

> BeneficiarioResponseDTO actualizarBeneficiario(socioId, id, updateBeneficiarioRequestDTO)

Actualizar un beneficiario

### Example

```ts
import {
  Configuration,
  BeneficiariosApi,
} from '';
import type { ActualizarBeneficiarioRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new BeneficiariosApi();

  const body = {
    // string
    socioId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
    // string
    id: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
    // UpdateBeneficiarioRequestDTO
    updateBeneficiarioRequestDTO: ...,
  } satisfies ActualizarBeneficiarioRequest;

  try {
    const data = await api.actualizarBeneficiario(body);
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
| **socioId** | `string` |  | [Defaults to `undefined`] |
| **id** | `string` |  | [Defaults to `undefined`] |
| **updateBeneficiarioRequestDTO** | [UpdateBeneficiarioRequestDTO](UpdateBeneficiarioRequestDTO.md) |  | |

### Return type

[**BeneficiarioResponseDTO**](BeneficiarioResponseDTO.md)

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


## crearBeneficiario

> BeneficiarioResponseDTO crearBeneficiario(socioId, createBeneficiarioRequestDTO)

Crear un nuevo beneficiario

### Example

```ts
import {
  Configuration,
  BeneficiariosApi,
} from '';
import type { CrearBeneficiarioRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new BeneficiariosApi();

  const body = {
    // string
    socioId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
    // CreateBeneficiarioRequestDTO
    createBeneficiarioRequestDTO: ...,
  } satisfies CrearBeneficiarioRequest;

  try {
    const data = await api.crearBeneficiario(body);
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
| **socioId** | `string` |  | [Defaults to `undefined`] |
| **createBeneficiarioRequestDTO** | [CreateBeneficiarioRequestDTO](CreateBeneficiarioRequestDTO.md) |  | |

### Return type

[**BeneficiarioResponseDTO**](BeneficiarioResponseDTO.md)

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


## eliminarBeneficiario

> DeleteBeneficiarioResponseDTO eliminarBeneficiario(socioId, id)

Eliminar un beneficiario (soft delete)

### Example

```ts
import {
  Configuration,
  BeneficiariosApi,
} from '';
import type { EliminarBeneficiarioRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new BeneficiariosApi();

  const body = {
    // string
    socioId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
    // string
    id: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies EliminarBeneficiarioRequest;

  try {
    const data = await api.eliminarBeneficiario(body);
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
| **socioId** | `string` |  | [Defaults to `undefined`] |
| **id** | `string` |  | [Defaults to `undefined`] |

### Return type

[**DeleteBeneficiarioResponseDTO**](DeleteBeneficiarioResponseDTO.md)

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


## listarBeneficiarios

> BeneficiarioListResponseDTO listarBeneficiarios(socioId)

Listar beneficiarios activos de un socio

### Example

```ts
import {
  Configuration,
  BeneficiariosApi,
} from '';
import type { ListarBeneficiariosRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new BeneficiariosApi();

  const body = {
    // string
    socioId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies ListarBeneficiariosRequest;

  try {
    const data = await api.listarBeneficiarios(body);
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
| **socioId** | `string` |  | [Defaults to `undefined`] |

### Return type

[**BeneficiarioListResponseDTO**](BeneficiarioListResponseDTO.md)

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


## obtenerBeneficiario

> BeneficiarioResponseDTO obtenerBeneficiario(socioId, id)

Obtener un beneficiario por ID

### Example

```ts
import {
  Configuration,
  BeneficiariosApi,
} from '';
import type { ObtenerBeneficiarioRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new BeneficiariosApi();

  const body = {
    // string
    socioId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
    // string
    id: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies ObtenerBeneficiarioRequest;

  try {
    const data = await api.obtenerBeneficiario(body);
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
| **socioId** | `string` |  | [Defaults to `undefined`] |
| **id** | `string` |  | [Defaults to `undefined`] |

### Return type

[**BeneficiarioResponseDTO**](BeneficiarioResponseDTO.md)

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

