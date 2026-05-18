# AhorrosApi

All URIs are relative to *http://localhost:18080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**calcularRendimiento**](AhorrosApi.md#calcularrendimientooperation) | **POST** /api/v1/cuentas/{numeroCuenta}/rendimientos/calcular | Calcular rendimiento de cuenta |
| [**calcularRendimientosBatch**](AhorrosApi.md#calcularrendimientosbatch) | **POST** /api/v1/cuentas/rendimientos/calcular-batch | Calcular rendimientos en batch |
| [**cerrarCuenta**](AhorrosApi.md#cerrarcuenta) | **DELETE** /api/v1/cuentas/{numeroCuenta} | Cerrar cuenta de ahorro |
| [**consultarSaldo**](AhorrosApi.md#consultarsaldo) | **GET** /api/v1/cuentas/{numeroCuenta}/saldo | Consultar saldo de cuenta |
| [**crearCuenta**](AhorrosApi.md#crearcuenta) | **POST** /api/v1/cuentas | Crear cuenta de ahorro |
| [**listarCuentasPorSocio**](AhorrosApi.md#listarcuentasporsocio) | **GET** /api/v1/cuentas/socio/{socioId} | Listar cuentas por socio |
| [**listarMovimientos**](AhorrosApi.md#listarmovimientos) | **GET** /api/v1/cuentas/{numeroCuenta}/movimientos | Listar movimientos de cuenta |
| [**listarRendimientos**](AhorrosApi.md#listarrendimientos) | **GET** /api/v1/cuentas/{numeroCuenta}/rendimientos | Listar rendimientos de cuenta |
| [**obtenerCuenta**](AhorrosApi.md#obtenercuenta) | **GET** /api/v1/cuentas/{numeroCuenta} | Consultar cuenta por número |
| [**obtenerMovimientoDetalle**](AhorrosApi.md#obtenermovimientodetalle) | **GET** /api/v1/cuentas/{numeroCuenta}/movimientos/{numeroOperacion} | Obtener detalle de movimiento |
| [**realizarDeposito**](AhorrosApi.md#realizardeposito) | **POST** /api/v1/cuentas/{numeroCuenta}/depositos | Realizar depósito |
| [**realizarRetiro**](AhorrosApi.md#realizarretiro) | **POST** /api/v1/cuentas/{numeroCuenta}/retiros | Realizar retiro |



## calcularRendimiento

> RendimientoResponse calcularRendimiento(numeroCuenta, calcularRendimientoRequest)

Calcular rendimiento de cuenta

### Example

```ts
import {
  Configuration,
  AhorrosApi,
} from '';
import type { CalcularRendimientoOperationRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AhorrosApi();

  const body = {
    // string
    numeroCuenta: numeroCuenta_example,
    // CalcularRendimientoRequest
    calcularRendimientoRequest: ...,
  } satisfies CalcularRendimientoOperationRequest;

  try {
    const data = await api.calcularRendimiento(body);
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
| **numeroCuenta** | `string` |  | [Defaults to `undefined`] |
| **calcularRendimientoRequest** | [CalcularRendimientoRequest](CalcularRendimientoRequest.md) |  | |

### Return type

[**RendimientoResponse**](RendimientoResponse.md)

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


## calcularRendimientosBatch

> CalcularBatchResponse calcularRendimientosBatch(calcularBatchRequest)

Calcular rendimientos en batch

### Example

```ts
import {
  Configuration,
  AhorrosApi,
} from '';
import type { CalcularRendimientosBatchRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AhorrosApi();

  const body = {
    // CalcularBatchRequest
    calcularBatchRequest: ...,
  } satisfies CalcularRendimientosBatchRequest;

  try {
    const data = await api.calcularRendimientosBatch(body);
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
| **calcularBatchRequest** | [CalcularBatchRequest](CalcularBatchRequest.md) |  | |

### Return type

[**CalcularBatchResponse**](CalcularBatchResponse.md)

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


## cerrarCuenta

> CerrarCuentaResponse cerrarCuenta(numeroCuenta)

Cerrar cuenta de ahorro

### Example

```ts
import {
  Configuration,
  AhorrosApi,
} from '';
import type { CerrarCuentaRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AhorrosApi();

  const body = {
    // string
    numeroCuenta: numeroCuenta_example,
  } satisfies CerrarCuentaRequest;

  try {
    const data = await api.cerrarCuenta(body);
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
| **numeroCuenta** | `string` |  | [Defaults to `undefined`] |

### Return type

[**CerrarCuentaResponse**](CerrarCuentaResponse.md)

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


## consultarSaldo

> SaldoResponse consultarSaldo(numeroCuenta)

Consultar saldo de cuenta

### Example

```ts
import {
  Configuration,
  AhorrosApi,
} from '';
import type { ConsultarSaldoRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AhorrosApi();

  const body = {
    // string
    numeroCuenta: numeroCuenta_example,
  } satisfies ConsultarSaldoRequest;

  try {
    const data = await api.consultarSaldo(body);
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
| **numeroCuenta** | `string` |  | [Defaults to `undefined`] |

### Return type

[**SaldoResponse**](SaldoResponse.md)

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


## crearCuenta

> CuentaAhorroResponse crearCuenta(createCuentaAhorroRequest)

Crear cuenta de ahorro

### Example

```ts
import {
  Configuration,
  AhorrosApi,
} from '';
import type { CrearCuentaRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AhorrosApi();

  const body = {
    // CreateCuentaAhorroRequest
    createCuentaAhorroRequest: ...,
  } satisfies CrearCuentaRequest;

  try {
    const data = await api.crearCuenta(body);
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
| **createCuentaAhorroRequest** | [CreateCuentaAhorroRequest](CreateCuentaAhorroRequest.md) |  | |

### Return type

[**CuentaAhorroResponse**](CuentaAhorroResponse.md)

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


## listarCuentasPorSocio

> CuentasPorSocioResponse listarCuentasPorSocio(socioId)

Listar cuentas por socio

### Example

```ts
import {
  Configuration,
  AhorrosApi,
} from '';
import type { ListarCuentasPorSocioRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AhorrosApi();

  const body = {
    // string
    socioId: 38400000-8cf0-11bd-b23e-10b96e4ef00d,
  } satisfies ListarCuentasPorSocioRequest;

  try {
    const data = await api.listarCuentasPorSocio(body);
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

[**CuentasPorSocioResponse**](CuentasPorSocioResponse.md)

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


## listarMovimientos

> MovimientosListResponse listarMovimientos(numeroCuenta, page, size, fechaInicio, fechaFin)

Listar movimientos de cuenta

### Example

```ts
import {
  Configuration,
  AhorrosApi,
} from '';
import type { ListarMovimientosRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AhorrosApi();

  const body = {
    // string
    numeroCuenta: numeroCuenta_example,
    // number (optional)
    page: 56,
    // number (optional)
    size: 56,
    // Date (optional)
    fechaInicio: 2013-10-20,
    // Date (optional)
    fechaFin: 2013-10-20,
  } satisfies ListarMovimientosRequest;

  try {
    const data = await api.listarMovimientos(body);
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
| **numeroCuenta** | `string` |  | [Defaults to `undefined`] |
| **page** | `number` |  | [Optional] [Defaults to `0`] |
| **size** | `number` |  | [Optional] [Defaults to `20`] |
| **fechaInicio** | `Date` |  | [Optional] [Defaults to `undefined`] |
| **fechaFin** | `Date` |  | [Optional] [Defaults to `undefined`] |

### Return type

[**MovimientosListResponse**](MovimientosListResponse.md)

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


## listarRendimientos

> RendimientosListResponse listarRendimientos(numeroCuenta, page, size)

Listar rendimientos de cuenta

### Example

```ts
import {
  Configuration,
  AhorrosApi,
} from '';
import type { ListarRendimientosRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AhorrosApi();

  const body = {
    // string
    numeroCuenta: numeroCuenta_example,
    // number (optional)
    page: 56,
    // number (optional)
    size: 56,
  } satisfies ListarRendimientosRequest;

  try {
    const data = await api.listarRendimientos(body);
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
| **numeroCuenta** | `string` |  | [Defaults to `undefined`] |
| **page** | `number` |  | [Optional] [Defaults to `0`] |
| **size** | `number` |  | [Optional] [Defaults to `20`] |

### Return type

[**RendimientosListResponse**](RendimientosListResponse.md)

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


## obtenerCuenta

> CuentaAhorroResponse obtenerCuenta(numeroCuenta)

Consultar cuenta por número

### Example

```ts
import {
  Configuration,
  AhorrosApi,
} from '';
import type { ObtenerCuentaRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AhorrosApi();

  const body = {
    // string
    numeroCuenta: numeroCuenta_example,
  } satisfies ObtenerCuentaRequest;

  try {
    const data = await api.obtenerCuenta(body);
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
| **numeroCuenta** | `string` |  | [Defaults to `undefined`] |

### Return type

[**CuentaAhorroResponse**](CuentaAhorroResponse.md)

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


## obtenerMovimientoDetalle

> MovimientoResponse obtenerMovimientoDetalle(numeroCuenta, numeroOperacion)

Obtener detalle de movimiento

### Example

```ts
import {
  Configuration,
  AhorrosApi,
} from '';
import type { ObtenerMovimientoDetalleRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AhorrosApi();

  const body = {
    // string
    numeroCuenta: numeroCuenta_example,
    // string
    numeroOperacion: numeroOperacion_example,
  } satisfies ObtenerMovimientoDetalleRequest;

  try {
    const data = await api.obtenerMovimientoDetalle(body);
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
| **numeroCuenta** | `string` |  | [Defaults to `undefined`] |
| **numeroOperacion** | `string` |  | [Defaults to `undefined`] |

### Return type

[**MovimientoResponse**](MovimientoResponse.md)

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


## realizarDeposito

> MovimientoResponse realizarDeposito(numeroCuenta, depositoRequest)

Realizar depósito

### Example

```ts
import {
  Configuration,
  AhorrosApi,
} from '';
import type { RealizarDepositoRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AhorrosApi();

  const body = {
    // string
    numeroCuenta: numeroCuenta_example,
    // DepositoRequest
    depositoRequest: ...,
  } satisfies RealizarDepositoRequest;

  try {
    const data = await api.realizarDeposito(body);
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
| **numeroCuenta** | `string` |  | [Defaults to `undefined`] |
| **depositoRequest** | [DepositoRequest](DepositoRequest.md) |  | |

### Return type

[**MovimientoResponse**](MovimientoResponse.md)

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


## realizarRetiro

> MovimientoResponse realizarRetiro(numeroCuenta, retiroRequest)

Realizar retiro

### Example

```ts
import {
  Configuration,
  AhorrosApi,
} from '';
import type { RealizarRetiroRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AhorrosApi();

  const body = {
    // string
    numeroCuenta: numeroCuenta_example,
    // RetiroRequest
    retiroRequest: ...,
  } satisfies RealizarRetiroRequest;

  try {
    const data = await api.realizarRetiro(body);
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
| **numeroCuenta** | `string` |  | [Defaults to `undefined`] |
| **retiroRequest** | [RetiroRequest](RetiroRequest.md) |  | |

### Return type

[**MovimientoResponse**](MovimientoResponse.md)

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

