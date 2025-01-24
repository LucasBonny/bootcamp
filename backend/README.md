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

![result](assets/image.png)

## Associação muitos para muitos

Na utilização do `@ManyToMany` temos que criar uma tabela intermediaria, que irá armazenar os ids das categorias e dos produtos.

Para fazer a associação, eu criei uma coleção do tipo `Set` para armazenar os ids das categorias, pois o set é uma coleção que armazena elementos únicos, e o que eu quero é que os elementos sejam únicos entre si, ou seja, não podem repetir.

![model](assets/image-2.png)

```java
//Entity: Product
@ManyToMany

//Cria uma tabela intermediaria
@JoinTable(name = "tb_product_category", 

//Pega a classe atual
joinColumns = @JoinColumn(name = "product_id"), 

//Pega o que estiver na coleção
inverseJoinColumns = @JoinColumn(name = "category_id"))
Set<Category> categories = new HashSet<>();

//Ao fazer isso o Set já tem como identifcar que seria a classe Category que será associada, pois ela herda de Category.
```

