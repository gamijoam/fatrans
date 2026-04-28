# AuthApi

All URIs are relative to *http://localhost:18080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**crearUsuario**](AuthApi.md#crearusuario) | **POST** /api/v1/auth/crear-usuario | Crear usuario vinculado a socio |
| [**getUsuarioActual**](AuthApi.md#getusuarioactual) | **GET** /api/v1/auth/me | Obtener usuario actual |
| [**login**](AuthApi.md#login) | **POST** /api/v1/auth/login | Iniciar sesión |
| [**loginWeb**](AuthApi.md#loginweb) | **POST** /api/v1/auth/login-web | Iniciar sesión desde Flutter Web |
| [**logout**](AuthApi.md#logout) | **POST** /api/v1/auth/logout | Cerrar sesión |
| [**logoutWeb**](AuthApi.md#logoutweb) | **POST** /api/v1/auth/logout-web | Cerrar sesión desde Flutter Web |
| [**recuperarPassword**](AuthApi.md#recuperarpassword) | **POST** /api/v1/auth/recuperar-password | Solicitar recuperación de contraseña |
| [**refreshToken**](AuthApi.md#refreshtoken) | **POST** /api/v1/auth/refresh | Refrescar token |
| [**refreshTokenWeb**](AuthApi.md#refreshtokenweb) | **POST** /api/v1/auth/refresh-web | Refrescar token desde Flutter Web |
| [**resetPassword**](AuthApi.md#resetpassword) | **POST** /api/v1/auth/reset-password | Restablecer contraseña |
| [**validarToken**](AuthApi.md#validartoken) | **POST** /api/v1/auth/validar | Validar token |



## crearUsuario

> CrearUsuarioResponseDTO crearUsuario(crearUsuarioRequestDTO)

Crear usuario vinculado a socio

Vincula un Socio existente con credenciales de acceso (Usuario)

### Example

```ts
import {
  Configuration,
  AuthApi,
} from '';
import type { CrearUsuarioRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AuthApi();

  const body = {
    // CrearUsuarioRequestDTO
    crearUsuarioRequestDTO: ...,
  } satisfies CrearUsuarioRequest;

  try {
    const data = await api.crearUsuario(body);
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
| **crearUsuarioRequestDTO** | [CrearUsuarioRequestDTO](CrearUsuarioRequestDTO.md) |  | |

### Return type

[**CrearUsuarioResponseDTO**](CrearUsuarioResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **409** | Nombre de usuario ya existe o socio ya tiene usuario vinculado |  -  |
| **201** | Usuario creado exitosamente |  -  |
| **404** | Socio no encontrado |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## getUsuarioActual

> UsuarioDTO getUsuarioActual()

Obtener usuario actual

Devuelve la información del usuario autenticado

### Example

```ts
import {
  Configuration,
  AuthApi,
} from '';
import type { GetUsuarioActualRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AuthApi();

  try {
    const data = await api.getUsuarioActual();
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

[**UsuarioDTO**](UsuarioDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **401** | No autorizado |  -  |
| **200** | Usuario obtenido exitosamente |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## login

> LoginResponseDTO login(loginRequestDTO)

Iniciar sesión

Autentica un usuario y devuelve tokens JWT

### Example

```ts
import {
  Configuration,
  AuthApi,
} from '';
import type { LoginRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AuthApi();

  const body = {
    // LoginRequestDTO
    loginRequestDTO: ...,
  } satisfies LoginRequest;

  try {
    const data = await api.login(body);
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
| **loginRequestDTO** | [LoginRequestDTO](LoginRequestDTO.md) |  | |

### Return type

[**LoginResponseDTO**](LoginResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **429** | Demasiadas solicitudes |  -  |
| **403** | Cuenta bloqueada o desactivada |  -  |
| **401** | Credenciales inválidas |  -  |
| **200** | Login exitoso |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## loginWeb

> LoginWebResponseDTO loginWeb(loginRequestDTO)

Iniciar sesión desde Flutter Web

Autentica y devuelve tokens en cookies httpOnly para Flutter Web

### Example

```ts
import {
  Configuration,
  AuthApi,
} from '';
import type { LoginWebRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AuthApi();

  const body = {
    // LoginRequestDTO
    loginRequestDTO: ...,
  } satisfies LoginWebRequest;

  try {
    const data = await api.loginWeb(body);
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
| **loginRequestDTO** | [LoginRequestDTO](LoginRequestDTO.md) |  | |

### Return type

[**LoginWebResponseDTO**](LoginWebResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **403** | Cuenta bloqueada o desactivada |  -  |
| **401** | Credenciales inválidas |  -  |
| **200** | Login exitoso con cookies |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## logout

> { [key: string]: string; } logout(authorization)

Cerrar sesión

Invalida la sesión actual del usuario

### Example

```ts
import {
  Configuration,
  AuthApi,
} from '';
import type { LogoutRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AuthApi();

  const body = {
    // string
    authorization: authorization_example,
  } satisfies LogoutRequest;

  try {
    const data = await api.logout(body);
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
| **authorization** | `string` |  | [Defaults to `undefined`] |

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
| **401** | No autorizado |  -  |
| **200** | Sesión cerrada exitosamente |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## logoutWeb

> { [key: string]: string; } logoutWeb()

Cerrar sesión desde Flutter Web

Invalida la sesión y limpia las cookies httpOnly

### Example

```ts
import {
  Configuration,
  AuthApi,
} from '';
import type { LogoutWebRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AuthApi();

  try {
    const data = await api.logoutWeb();
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

**{ [key: string]: string; }**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Sesión cerrada y cookies eliminadas |  -  |
| **401** | No autorizado |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## recuperarPassword

> MensajeResponseDTO recuperarPassword(recuperarPasswordRequestDTO)

Solicitar recuperación de contraseña

Genera token de recuperación y envía email con enlace

### Example

```ts
import {
  Configuration,
  AuthApi,
} from '';
import type { RecuperarPasswordRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AuthApi();

  const body = {
    // RecuperarPasswordRequestDTO
    recuperarPasswordRequestDTO: ...,
  } satisfies RecuperarPasswordRequest;

  try {
    const data = await api.recuperarPassword(body);
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
| **recuperarPasswordRequestDTO** | [RecuperarPasswordRequestDTO](RecuperarPasswordRequestDTO.md) |  | |

### Return type

[**MensajeResponseDTO**](MensajeResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Si el email existe, se ha enviado un enlace de recuperación |  -  |
| **400** | Solicitud inválida |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## refreshToken

> LoginResponseDTO refreshToken(refreshTokenRequestDTO)

Refrescar token

Obtiene nuevos tokens usando un refresh token válido

### Example

```ts
import {
  Configuration,
  AuthApi,
} from '';
import type { RefreshTokenRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AuthApi();

  const body = {
    // RefreshTokenRequestDTO
    refreshTokenRequestDTO: ...,
  } satisfies RefreshTokenRequest;

  try {
    const data = await api.refreshToken(body);
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
| **refreshTokenRequestDTO** | [RefreshTokenRequestDTO](RefreshTokenRequestDTO.md) |  | |

### Return type

[**LoginResponseDTO**](LoginResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **401** | Refresh token inválido o expirado |  -  |
| **200** | Token refrescado exitosamente |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## refreshTokenWeb

> LoginWebResponseDTO refreshTokenWeb()

Refrescar token desde Flutter Web

Usa cookie httpOnly para refresh y devuelve nuevas cookies

### Example

```ts
import {
  Configuration,
  AuthApi,
} from '';
import type { RefreshTokenWebRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AuthApi();

  try {
    const data = await api.refreshTokenWeb();
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

[**LoginWebResponseDTO**](LoginWebResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **401** | Refresh token inválido o expirado |  -  |
| **200** | Tokens actualizados |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## resetPassword

> MensajeResponseDTO resetPassword(resetPasswordRequestDTO)

Restablecer contraseña

Restablece la contraseña usando un token de recuperación válido

### Example

```ts
import {
  Configuration,
  AuthApi,
} from '';
import type { ResetPasswordRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AuthApi();

  const body = {
    // ResetPasswordRequestDTO
    resetPasswordRequestDTO: ...,
  } satisfies ResetPasswordRequest;

  try {
    const data = await api.resetPassword(body);
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
| **resetPasswordRequestDTO** | [ResetPasswordRequestDTO](ResetPasswordRequestDTO.md) |  | |

### Return type

[**MensajeResponseDTO**](MensajeResponseDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Contraseña actualizada exitosamente |  -  |
| **400** | Token inválido, expirado o password no cumple requisitos |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## validarToken

> TokenValidacionDTO validarToken(authorization)

Validar token

Verifica si un token JWT es válido

### Example

```ts
import {
  Configuration,
  AuthApi,
} from '';
import type { ValidarTokenRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AuthApi();

  const body = {
    // string
    authorization: authorization_example,
  } satisfies ValidarTokenRequest;

  try {
    const data = await api.validarToken(body);
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
| **authorization** | `string` |  | [Defaults to `undefined`] |

### Return type

[**TokenValidacionDTO**](TokenValidacionDTO.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `*/*`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Token válido |  -  |
| **401** | Token inválido o expirado |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

