# AdminDashboardApi

All URIs are relative to *http://localhost:18080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**obtenerEstadisticas1**](AdminDashboardApi.md#obtenerestadisticas1) | **GET** /api/v1/admin/dashboard/estadisticas | Obtener estadísticas completas del dashboard |



## obtenerEstadisticas1

> DashboardEstadisticasResponse obtenerEstadisticas1()

Obtener estadísticas completas del dashboard

### Example

```ts
import {
  Configuration,
  AdminDashboardApi,
} from '';
import type { ObtenerEstadisticas1Request } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AdminDashboardApi();

  try {
    const data = await api.obtenerEstadisticas1();
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

[**DashboardEstadisticasResponse**](DashboardEstadisticasResponse.md)

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

