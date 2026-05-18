# CrditosApi

All URIs are relative to *http://localhost:18080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**aprobarSolicitud1**](CrditosApi.md#aprobarsolicitud1) | **POST** /api/v1/creditos/solicitudes/{numeroSolicitud}/aprobar | Aprobar solicitud de crédito |
| [**crearSolicitud1**](CrditosApi.md#crearsolicitud1) | **POST** /api/v1/creditos/solicitudes | Crear solicitud de crédito |
| [**desembolsarCredito**](CrditosApi.md#desembolsarcredito) | **POST** /api/v1/creditos/solicitudes/{numeroSolicitud}/desembolso | Desembolsar crédito |
| [**evaluarSolicitud**](CrditosApi.md#evaluarsolicitudoperation) | **POST** /api/v1/creditos/solicitudes/{numeroSolicitud}/evaluar | Evaluar solicitud de crédito |
| [**listarCuotas**](CrditosApi.md#listarcuotas) | **GET** /api/v1/creditos/{numeroSolicitud}/cuotas | Listar cuotas del plan de amortización |
| [**listarSolicitudesPorSocio**](CrditosApi.md#listarsolicitudesporsocio) | **GET** /api/v1/creditos/solicitudes/socio/{socioId} | Listar solicitudes de crédito por socio |
| [**listarTiposCredito**](CrditosApi.md#listartiposcredito) | **GET** /api/v1/creditos/tipos-credito | Listar tipos de crédito disponibles |
| [**listarTodasSolicitudes**](CrditosApi.md#listartodassolicitudes) | **GET** /api/v1/admin/creditos/solicitudes | Listar todas las solicitudes de crédito (Admin) |
| [**obtenerEstadoCredito**](CrditosApi.md#obtenerestadocredito) | **GET** /api/v1/creditos/{numeroSolicitud} | Consultar estado de crédito |
| [**obtenerPlanAmortizacion**](CrditosApi.md#obtenerplanamortizacion) | **GET** /api/v1/creditos/solicitudes/{numeroSolicitud}/plan | Consultar plan de amortización |
| [**obtenerSolicitud**](CrditosApi.md#obtenersolicitud) | **GET** /api/v1/creditos/solicitudes/{numeroSolicitud} | Consultar solicitud de crédito |
| [**obtenerTipoCredito**](CrditosApi.md#obtenertipocredito) | **GET** /api/v1/creditos/tipos-credito/{id} | Consultar tipo de crédito específico |
| [**rechazarSolicitud1**](CrditosApi.md#rechazarsolicitud1) | **POST** /api/v1/creditos/solicitudes/{numeroSolicitud}/rechazar | Rechazar solicitud de crédito |
| [**registrarPagoCuota**](CrditosApi.md#registrarpagocuota) | **POST** /api/v1/creditos/cuotas/{cuotaId}/pago | Registrar pago de cuota |
| [**simularCredito**](CrditosApi.md#simularcredito) | **POST** /api/v1/simulador | Simular crédito (rate limited) |



## aprobarSolicitud1

> { [key: string]: object; } aprobarSolicitud1(numeroSolicitud, aprobarRechazarRequest)

Aprobar solicitud de crédito

### Example

```ts
import {
  Configuration,
  CrditosApi,
} from '';
import type { AprobarSolicitud1Request } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new CrditosApi();

  const body = {
    // string
    numeroSolicitud: numeroSolicitud_example,
    // AprobarRechazarRequest
    aprobarRechazarRequest: ...,
  } satisfies AprobarSolicitud1Request;

  try {
    const data = await api.aprobarSolicitud1(body);
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
| **numeroSolicitud** | `string` |  | [Defaults to `undefined`] |
| **aprobarRechazarRequest** | [AprobarRechazarRequest](AprobarRechazarRequest.md) |  | |

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


## crearSolicitud1

> SolicitudCreditoResponse crearSolicitud1(crearSolicitudCreditoRequest)

Crear solicitud de crédito

### Example

```ts
import {
  Configuration,
  CrditosApi,
} from '';
import type { CrearSolicitud1Request } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new CrditosApi();

  const body = {
    // CrearSolicitudCreditoRequest
    crearSolicitudCreditoRequest: ...,
  } satisfies CrearSolicitud1Request;

  try {
    const data = await api.crearSolicitud1(body);
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
| **crearSolicitudCreditoRequest** | [CrearSolicitudCreditoRequest](CrearSolicitudCreditoRequest.md) |  | |

### Return type

[**SolicitudCreditoResponse**](SolicitudCreditoResponse.md)

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


## desembolsarCredito

> { [key: string]: object; } desembolsarCredito(numeroSolicitud, desembolsaRequest)

Desembolsar crédito

### Example

```ts
import {
  Configuration,
  CrditosApi,
} from '';
import type { DesembolsarCreditoRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new CrditosApi();

  const body = {
    // string
    numeroSolicitud: numeroSolicitud_example,
    // DesembolsaRequest
    desembolsaRequest: ...,
  } satisfies DesembolsarCreditoRequest;

  try {
    const data = await api.desembolsarCredito(body);
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
| **numeroSolicitud** | `string` |  | [Defaults to `undefined`] |
| **desembolsaRequest** | [DesembolsaRequest](DesembolsaRequest.md) |  | |

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


## evaluarSolicitud

> EvaluacionResponse evaluarSolicitud(numeroSolicitud, evaluarSolicitudRequest)

Evaluar solicitud de crédito

### Example

```ts
import {
  Configuration,
  CrditosApi,
} from '';
import type { EvaluarSolicitudOperationRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new CrditosApi();

  const body = {
    // string
    numeroSolicitud: numeroSolicitud_example,
    // EvaluarSolicitudRequest
    evaluarSolicitudRequest: ...,
  } satisfies EvaluarSolicitudOperationRequest;

  try {
    const data = await api.evaluarSolicitud(body);
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
| **numeroSolicitud** | `string` |  | [Defaults to `undefined`] |
| **evaluarSolicitudRequest** | [EvaluarSolicitudRequest](EvaluarSolicitudRequest.md) |  | |

### Return type

[**EvaluacionResponse**](EvaluacionResponse.md)

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


## listarCuotas

> { [key: string]: object; } listarCuotas(numeroSolicitud, page, size, estado)

Listar cuotas del plan de amortización

### Example

```ts
import {
  Configuration,
  CrditosApi,
} from '';
import type { ListarCuotasRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new CrditosApi();

  const body = {
    // string
    numeroSolicitud: numeroSolicitud_example,
    // number (optional)
    page: 56,
    // number (optional)
    size: 56,
    // 'PENDIENTE' | 'PAGADA' | 'VENCIDA' | 'CURSO_MORA' | 'CANCELADA' | 'EJECUTADA' (optional)
    estado: estado_example,
  } satisfies ListarCuotasRequest;

  try {
    const data = await api.listarCuotas(body);
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
| **numeroSolicitud** | `string` |  | [Defaults to `undefined`] |
| **page** | `number` |  | [Optional] [Defaults to `0`] |
| **size** | `number` |  | [Optional] [Defaults to `12`] |
| **estado** | `PENDIENTE`, `PAGADA`, `VENCIDA`, `CURSO_MORA`, `CANCELADA`, `EJECUTADA` |  | [Optional] [Defaults to `undefined`] [Enum: PENDIENTE, PAGADA, VENCIDA, CURSO_MORA, CANCELADA, EJECUTADA] |

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


## listarSolicitudesPorSocio

> { [key: string]: object; } listarSolicitudesPorSocio(socioId)

Listar solicitudes de crédito por socio

### Example

```ts
import {
  Configuration,
  CrditosApi,
} from '';
import type { ListarSolicitudesPorSocioRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new CrditosApi();

  const body = {
    // string
    socioId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies ListarSolicitudesPorSocioRequest;

  try {
    const data = await api.listarSolicitudesPorSocio(body);
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


## listarTiposCredito

> { [key: string]: object; } listarTiposCredito(page, size)

Listar tipos de crédito disponibles

### Example

```ts
import {
  Configuration,
  CrditosApi,
} from '';
import type { ListarTiposCreditoRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new CrditosApi();

  const body = {
    // number (optional)
    page: 56,
    // number (optional)
    size: 56,
  } satisfies ListarTiposCreditoRequest;

  try {
    const data = await api.listarTiposCredito(body);
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
| **size** | `number` |  | [Optional] [Defaults to `20`] |

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


## listarTodasSolicitudes

> PageSolicitudCreditoAdminResponse listarTodasSolicitudes(estado, fechaDesde, fechaHasta, montoMin, montoMax, page, size, sortBy, sortDir)

Listar todas las solicitudes de crédito (Admin)

### Example

```ts
import {
  Configuration,
  CrditosApi,
} from '';
import type { ListarTodasSolicitudesRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new CrditosApi();

  const body = {
    // 'PENDIENTE' | 'EN_EVALUACION' | 'APROBADA' | 'RECHAZADA' | 'CANCELADA' | 'DESEMBOLSADO' | 'COLATERAL_EJECUTADO' (optional)
    estado: estado_example,
    // Date (optional)
    fechaDesde: 2013-10-20T19:20:30+01:00,
    // Date (optional)
    fechaHasta: 2013-10-20T19:20:30+01:00,
    // number (optional)
    montoMin: 8.14,
    // number (optional)
    montoMax: 8.14,
    // number (optional)
    page: 56,
    // number (optional)
    size: 56,
    // string (optional)
    sortBy: sortBy_example,
    // string (optional)
    sortDir: sortDir_example,
  } satisfies ListarTodasSolicitudesRequest;

  try {
    const data = await api.listarTodasSolicitudes(body);
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
| **estado** | `PENDIENTE`, `EN_EVALUACION`, `APROBADA`, `RECHAZADA`, `CANCELADA`, `DESEMBOLSADO`, `COLATERAL_EJECUTADO` |  | [Optional] [Defaults to `undefined`] [Enum: PENDIENTE, EN_EVALUACION, APROBADA, RECHAZADA, CANCELADA, DESEMBOLSADO, COLATERAL_EJECUTADO] |
| **fechaDesde** | `Date` |  | [Optional] [Defaults to `undefined`] |
| **fechaHasta** | `Date` |  | [Optional] [Defaults to `undefined`] |
| **montoMin** | `number` |  | [Optional] [Defaults to `undefined`] |
| **montoMax** | `number` |  | [Optional] [Defaults to `undefined`] |
| **page** | `number` |  | [Optional] [Defaults to `0`] |
| **size** | `number` |  | [Optional] [Defaults to `20`] |
| **sortBy** | `string` |  | [Optional] [Defaults to `&#39;createdAt&#39;`] |
| **sortDir** | `string` |  | [Optional] [Defaults to `&#39;DESC&#39;`] |

### Return type

[**PageSolicitudCreditoAdminResponse**](PageSolicitudCreditoAdminResponse.md)

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


## obtenerEstadoCredito

> CreditoResponse obtenerEstadoCredito(numeroSolicitud)

Consultar estado de crédito

### Example

```ts
import {
  Configuration,
  CrditosApi,
} from '';
import type { ObtenerEstadoCreditoRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new CrditosApi();

  const body = {
    // string
    numeroSolicitud: numeroSolicitud_example,
  } satisfies ObtenerEstadoCreditoRequest;

  try {
    const data = await api.obtenerEstadoCredito(body);
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
| **numeroSolicitud** | `string` |  | [Defaults to `undefined`] |

### Return type

[**CreditoResponse**](CreditoResponse.md)

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


## obtenerPlanAmortizacion

> PlanAmortizacionResponse obtenerPlanAmortizacion(numeroSolicitud)

Consultar plan de amortización

### Example

```ts
import {
  Configuration,
  CrditosApi,
} from '';
import type { ObtenerPlanAmortizacionRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new CrditosApi();

  const body = {
    // string
    numeroSolicitud: numeroSolicitud_example,
  } satisfies ObtenerPlanAmortizacionRequest;

  try {
    const data = await api.obtenerPlanAmortizacion(body);
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
| **numeroSolicitud** | `string` |  | [Defaults to `undefined`] |

### Return type

[**PlanAmortizacionResponse**](PlanAmortizacionResponse.md)

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


## obtenerSolicitud

> SolicitudCreditoResponse obtenerSolicitud(numeroSolicitud)

Consultar solicitud de crédito

### Example

```ts
import {
  Configuration,
  CrditosApi,
} from '';
import type { ObtenerSolicitudRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new CrditosApi();

  const body = {
    // string
    numeroSolicitud: numeroSolicitud_example,
  } satisfies ObtenerSolicitudRequest;

  try {
    const data = await api.obtenerSolicitud(body);
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
| **numeroSolicitud** | `string` |  | [Defaults to `undefined`] |

### Return type

[**SolicitudCreditoResponse**](SolicitudCreditoResponse.md)

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


## obtenerTipoCredito

> TipoCreditoPublicResponse obtenerTipoCredito(id)

Consultar tipo de crédito específico

### Example

```ts
import {
  Configuration,
  CrditosApi,
} from '';
import type { ObtenerTipoCreditoRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new CrditosApi();

  const body = {
    // number
    id: 789,
  } satisfies ObtenerTipoCreditoRequest;

  try {
    const data = await api.obtenerTipoCredito(body);
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
| **id** | `number` |  | [Defaults to `undefined`] |

### Return type

[**TipoCreditoPublicResponse**](TipoCreditoPublicResponse.md)

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


## rechazarSolicitud1

> { [key: string]: object; } rechazarSolicitud1(numeroSolicitud, aprobarRechazarRequest)

Rechazar solicitud de crédito

### Example

```ts
import {
  Configuration,
  CrditosApi,
} from '';
import type { RechazarSolicitud1Request } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new CrditosApi();

  const body = {
    // string
    numeroSolicitud: numeroSolicitud_example,
    // AprobarRechazarRequest
    aprobarRechazarRequest: ...,
  } satisfies RechazarSolicitud1Request;

  try {
    const data = await api.rechazarSolicitud1(body);
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
| **numeroSolicitud** | `string` |  | [Defaults to `undefined`] |
| **aprobarRechazarRequest** | [AprobarRechazarRequest](AprobarRechazarRequest.md) |  | |

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


## registrarPagoCuota

> PagoCuotaResponse registrarPagoCuota(cuotaId, pagoCuotaRequest)

Registrar pago de cuota

### Example

```ts
import {
  Configuration,
  CrditosApi,
} from '';
import type { RegistrarPagoCuotaRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new CrditosApi();

  const body = {
    // string
    cuotaId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
    // PagoCuotaRequest
    pagoCuotaRequest: ...,
  } satisfies RegistrarPagoCuotaRequest;

  try {
    const data = await api.registrarPagoCuota(body);
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
| **cuotaId** | `string` |  | [Defaults to `undefined`] |
| **pagoCuotaRequest** | [PagoCuotaRequest](PagoCuotaRequest.md) |  | |

### Return type

[**PagoCuotaResponse**](PagoCuotaResponse.md)

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


## simularCredito

> SimulacionResponse simularCredito(simulacionRequest)

Simular crédito (rate limited)

### Example

```ts
import {
  Configuration,
  CrditosApi,
} from '';
import type { SimularCreditoRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new CrditosApi();

  const body = {
    // SimulacionRequest
    simulacionRequest: ...,
  } satisfies SimularCreditoRequest;

  try {
    const data = await api.simularCredito(body);
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
| **simulacionRequest** | [SimulacionRequest](SimulacionRequest.md) |  | |

### Return type

[**SimulacionResponse**](SimulacionResponse.md)

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

