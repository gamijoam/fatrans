# KYCAdminApi

All URIs are relative to *http://localhost:18080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**obtenerColaRevision**](KYCAdminApi.md#obtenercolarevision) | **GET** /api/v1/kyc/cola-revision | Obtener cola de revision de verificaciones |
| [**obtenerEstadisticas**](KYCAdminApi.md#obtenerestadisticas) | **GET** /api/v1/kyc/admin/estadisticas | Obtener estadisticas de KYC |
| [**obtenerHistorial**](KYCAdminApi.md#obtenerhistorial) | **GET** /api/v1/kyc/historial | Obtener historial de verificaciones del socio |



## obtenerColaRevision

> ColaRevisionResponse obtenerColaRevision(page, size, nivel, estado)

Obtener cola de revision de verificaciones

### Example

```ts
import {
  Configuration,
  KYCAdminApi,
} from '';
import type { ObtenerColaRevisionRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new KYCAdminApi();

  const body = {
    // number (optional)
    page: 56,
    // number (optional)
    size: 56,
    // 'BASICO' | 'MEDIO' | 'COMPLETO' (optional)
    nivel: nivel_example,
    // 'PENDIENTE' | 'EN_REVISION' | 'APROBADO' | 'RECHAZADO' | 'REENVIADO' | 'EXPIRADO' | 'CANCELADO' (optional)
    estado: estado_example,
  } satisfies ObtenerColaRevisionRequest;

  try {
    const data = await api.obtenerColaRevision(body);
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
| **nivel** | `BASICO`, `MEDIO`, `COMPLETO` |  | [Optional] [Defaults to `undefined`] [Enum: BASICO, MEDIO, COMPLETO] |
| **estado** | `PENDIENTE`, `EN_REVISION`, `APROBADO`, `RECHAZADO`, `REENVIADO`, `EXPIRADO`, `CANCELADO` |  | [Optional] [Defaults to `&#39;EN_REVISION&#39;`] [Enum: PENDIENTE, EN_REVISION, APROBADO, RECHAZADO, REENVIADO, EXPIRADO, CANCELADO] |

### Return type

[**ColaRevisionResponse**](ColaRevisionResponse.md)

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


## obtenerEstadisticas

> EstadisticasKYCResponse obtenerEstadisticas()

Obtener estadisticas de KYC

### Example

```ts
import {
  Configuration,
  KYCAdminApi,
} from '';
import type { ObtenerEstadisticasRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new KYCAdminApi();

  try {
    const data = await api.obtenerEstadisticas();
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**EstadisticasKYCResponse**](EstadisticasKYCResponse.md)

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


## obtenerHistorial

> HistorialKYCResponse obtenerHistorial()

Obtener historial de verificaciones del socio

### Example

```ts
import {
  Configuration,
  KYCAdminApi,
} from '';
import type { ObtenerHistorialRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new KYCAdminApi();

  try {
    const data = await api.obtenerHistorial();
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**HistorialKYCResponse**](HistorialKYCResponse.md)

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

