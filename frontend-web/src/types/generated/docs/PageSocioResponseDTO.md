
# PageSocioResponseDTO


## Properties

Name | Type
------------ | -------------
`totalElements` | number
`totalPages` | number
`pageable` | [PageableObject](PageableObject.md)
`first` | boolean
`last` | boolean
`numberOfElements` | number
`size` | number
`content` | [Array&lt;SocioResponseDTO&gt;](SocioResponseDTO.md)
`number` | number
`sort` | [Array&lt;SortObject&gt;](SortObject.md)
`empty` | boolean

## Example

```typescript
import type { PageSocioResponseDTO } from ''

// TODO: Update the object below with actual values
const example = {
  "totalElements": null,
  "totalPages": null,
  "pageable": null,
  "first": null,
  "last": null,
  "numberOfElements": null,
  "size": null,
  "content": null,
  "number": null,
  "sort": null,
  "empty": null,
} satisfies PageSocioResponseDTO

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as PageSocioResponseDTO
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


