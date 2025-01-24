# Bootcamp Spring

## Paginação

Para começar a usar a paginação, primeiro iremos trocar o retorno para Page<T>.

```java
@GetMapping
public ResponseEntity<Page<CategoryDTO>> findAll() {
    return ResponseEntity.ok().body(categoryService.findAll());
}
```
Quando for usar paginação, podemos usar alguns parâmetros padrões na URL:
```java
//O valor tem que ser o mesmo nome da variável que vai receber o dado.

//Numero da página (0..N)
@RequestParam(value = "page", defaultValue = "0") Integer page,
//Quantidade de itens por página (1..N)
@RequestParam(value = "linesPerPage", defaultValue = "12") Integer linesPerPage,
//Como será ordenado o retorno
@RequestParam(value = "orderBy", defaultValue = "name") String orderBy,
//Ordem de ordenação (ASC ou DESC)
@RequestParam(value = "direction", defaultValue = "ASC") String direction

//Se não informar nenhum parâmetro, o valor padrão será pré definido.
```

> [!IMPORTANT]
> Use o `@RequestParam` quando for usar dados opcionais na URL. Caso seja obrigatorio, use `@PathVariable`.

```java
// Erro bobo, conflito na importação
import org.springframework.data.domain.Sort.Direction;

@GetMapping
public ResponseEntity<Page<CategoryDTO>> findAll(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "12") Integer linesPerPage, @RequestParam(defaultValue = "name") String orderBy, @RequestParam(defaultValue = "ASC") String direction) {
    PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
    Page<CategoryDTO> list = categoryService.findAllPaged(pageRequest);
    return ResponseEntity.ok().body(list);
}
```

Já no service, precisamos alterar o findAll para receber a paginação.
```java
@Transactional(readOnly = true)
public Page<CategoryDTO> findAllPaged(PageRequest pageRequest) {
    Page<Category> list = categoryRepository.findAll(pageRequest);
    return list.map(CategoryDTO::new);
}
```

e com isso ja podemos usar a paginação no endpoint pelo postman:

```bash
GET http://localhost:8080/categories?page=1&linesPerPage=5&direction=DESC&orderBy=id
```

![alt text](assets/image.png)