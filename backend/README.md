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

## Paginação refatorada

### Parâmetros
- page = 0
- size = 12
- sort = name,ASC

```java
// Camada controller
@GetMapping
public ResponseEntity<Page<ProductDTO>> findAll(Pageable pageable) {
    Page<ProductDTO> list = productService.findAllPaged(pageable);
    return ResponseEntity.ok().body(list);
}   

```java
// Camada service
	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(Pageable pageable) {
		Page<Product> list = productRepository.findAll(pageable);
		return list.map(x -> {
			return new ProductDTO(x, new HashSet<>(x.getCategories()));
		});
	}

```

### Resultado

```bash
GET http://localhost:8080/products?page=0&size=5&sort=id,DESC
```

![result](assets/image-5.png)

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

## Capítulo 1 - CRUD 

### ProductDTO

O productDTO tem que ter todos os atributos da Product, mas também tem que ter as categorias, então vamos criar um construtor com parâmetros e um construtor para receber a coleção de categorias.
```java
//Construtor padrão
public ProductDTO() {
}

//Construtor com parâmetros
public ProductDTO(Long id, String name, String description, Double price, String imgUrl, Instant date) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.price = price;
    this.imgUrl = imgUrl;
    this.date = date;
}
//Copiar os atributos de outra classe para essa
public ProductDTO(Product entity) {
    BeanUtils.copyProperties(entity, this);
}
//Copiar os atributos de outra classe para essa e adicionar as categorias
public ProductDTO(Product entity, Set<Category> categories) {
    this(entity);
    categories.forEach(x -> this.categories.add(new CategoryDTO(x)));
}
```
### ProductService
O ProductService tem que ser alterado para retornar o ProductDTO com as categorias.
```java
@Transactional(readOnly = true)
public Page<ProductDTO> findAllPaged(PageRequest pageRequest) {
    Page<Product> list = productRepository.findAll(pageRequest);
    //criado uma forma de retornar o produto com as categorias
    return list.map(x -> {
        return new ProductDTO(x, new HashSet<>(x.getCategories()));
    });
}

@Transactional(readOnly = true)
public ProductDTO findById(Long id) {
    Optional<Product> list = productRepository.findById(id);
    //criado uma forma de retornar o produto com as categorias e se for nulo, lançar uma exception
    return list.map(x -> {
        return new ProductDTO(x, new HashSet<>(x.getCategories()));
    }).orElseThrow(() -> new ResourceNotFoundException("Id " + id + " not found!"));
}
```

### Resultados
#### findAll
![findAll](assets/image-3.png)
#### findById
![findById](assets/image-4.png)

### Insert, Update and Delete

Para fazer o insert do produto teremos que criar um objeto do tipo Product e adicionar um método `copyDtoToEntity` para copiar os dados do dto para o entity.

```java
@Autowired
private CategoryRepository categoryRepository;

private Product copyDtoToEntity(ProductDTO dto, Product entity) {
    entity.setName(dto.getName());
    entity.setDescription(dto.getDescription());
    entity.setPrice(dto.getPrice());
    entity.setImgUrl(dto.getImgUrl());
    entity.setDate(dto.getDate());
    entity.getCategories().clear();
    for(CategoryDTO d : dto.getCategories()) {
        Category cat = categoryRepository.getReferenceById(d.getId());
        entity.getCategories().add(cat);
    }
    return entity;
}
```
Fazendo dessa forma a entidade que será salva, terá as categorias que foram passadas no dto.

> [!IMPORTANT]
> Será necessário adicionar no retorno dos métodos o atributo `getCategories()` para receber nas requisicoes as categorias do produto.
> ```java
> return new ProductDTO(entity, entity.getCategories());
> ```


## Capítulo 2 - Testes automatizados

### Tipos de testes

- Teste unitário
- Teste de integração
- Teste Funcional

1. Teste unitário não pode depender de outros componentes do sistema, somente o componente que estiver sendo testado.
2. Teste de integração pode acessar todas as camadas do sistema para realizar os testes entre elas, e recursos externos.
3. Teste Funcional é um teste do ponto de vista do usuário para validar que o sistema responde de forma esperada.

### TDD (Test Driven Development)

O TDD(Desenvolvimento guiado pelos testes) é um método de desenvolvimento de software baseado em testes.

> [!TIP]
> Não é porque seu software tenha testes, que ele é baseado em TDD.
> A modelo de desenvolvimento baseada em TDD é a seguinte:
> 1. Escrever o teste
> 2. Executar o teste
> 3. Fazer o código
> 4. Refatorar o código (Opcional)

### Boas práticas

#### Nomeclatura
- <Ação> Should <Efeito> [When <Cenário>]
```java
//Delete Should Throw Resource Not Found Exception When Id Does Not Exist
public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {}
```
#### Padrão AAA
- Arrange - Instanciar dados necessários para realizar o teste
- Act - Executar o teste
- Assert - Validar o resultado

```java
@Test
public void saveSouldPersistWithAutoincrementWhenIdIsNull() {
    // Arrange
    Product product = new Product(26L,"Test1", "Test2", 123.0, "http://local/", Instant.now());
	product.setId(null);
    
    // Act
    product = repository.save(product);
    
    // Assert
    Assertions.assertNotNull(product.getId());
    Assertions.assertEquals(countTotalProducts + 1 , product.getId());
}
```

#### SOLID - Inversão de Dependência
- S - Single Responsibility
- O - Open/Closed
- L - Liskov Substitution
- I - Interface Segregation
- D - Dependency Inversion

Eu não posso depender da implementação de um componente dentro do meu componente atual, para isso será necessário utilizar o Mock para simular a implementação.

### Visão geral de JUnit 5

- O primeiro passo é criar uma classe de testes.
- A classe pode conter um ou mais métodos com anotação `@Test`.
- Um método `@Test` deve ser void.
- O objetivo é que todos os métodos com anotação `@Test` sejam executados sem falhas.
- O que define se o método passa ou falha são as Assertions.

### Assertions

É a forma utilizada para validar o resultado na aplicação com JUnit.

#### Membros da classe
- `assertTrue` - usado para validar o resultado (booleano)
- `assertFalse` - usado para validar o resultado (booleano)
- `assertNull` - serve para validar se o objeto é nulo
- `assertNotNull` - serve para validar se o objeto não é nulo
- `assertEquals` - serve para validar se o resultado é igual ao esperado
- `assertNotEquals` - serve para validar se o resultado é diferente ao esperado
- `assertSame` - serve para validar se o objeto é o mesmo
- `assertNotSame` - serve para validar se o objeto não é o mesmo
- `assertThrows` - serve para validar se o código lança uma exceção, na qual pode ter um modelo de implementação diferente das outras:
```java
@Test
public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
        service.delete(100L);
    });
}
```

### Fábrica de objetos 

Para evitar repetição de código, o ideal é criar uma classe de fabrica `tests.Factory` para criar objetos comuns, como o produto nesse caso.
```java
public static Product createProduct() {
    Product product = new Product(1L, "Phone", "Good Phone", 800.0, "https://img.com/img.png", Instant.parse("2020-10-20T03:00:00Z"));
    return product;
}
```
### Anotações

- `@SpringBootTest` - Indica que o teste é um teste de integração com o Spring Boot(Carrega todos os componentes do Spring Boot).

- `@AutoConfigureMockMvc` - Trata as requisições HTTP sem a necessidade de instanciar o servidor Tomcat, atribuido a anotação `@SpringBootTest`.

- `@WebMvcTest(Controller.class)` - Carrega somente o contexto da camada Web.

- `@ExtendWith(SpringExtension.class)` - Não carrega o contexto mas permite usar os recursos do Spring com o JUnit(service/component).

- `@DataJpaTest` - Carrega somente os componetes relacionados ao Spring Data JPA. Cada teste é transacional e dá rollback automaticamente no final().


### Fixtures

- `@BeforeAll` - Executado antes de todos os testes.

- `@AfterAll` - Executado depois de todos os testes.

- `@BeforeEach` - Executado antes de cada teste.

- `@AfterEach` - Executado depois de cada teste.

```java
//Modelo de teste
class TesteTest {

    //Antes de todos os testes
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	//Depois de todos os testes
	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	//Antes de cada teste
	@BeforeEach
	void setUp() throws Exception {
	}

	//Depois de cada teste
	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void test() {
		fail("Not yet implemented");
	}

}
```
### Mockito e Mock - Repository

#### Anotações

- `@InjectMocks` - Serve para simular o comportamento de um componente.
- `@Mock` - Serve para simular o comportamento de uma dependência dentro de um componente de teste.
- `@Test` - Define um método para ser executado como teste.

#### Mocks

```java
Mockito.when(repository.findAll((Pageable)ArgumentMatchers.any())).thenReturn(page); //Quando findAll(Pageable) for chamado retornar uma implementação de Page

Mockito.when(repository.findById(nonExistingId)).thenReturn(Optional.empty()); //Quando findById(Id Inexistente) for chamado, retornar Optional.empty()

Mockito.doNothing().when(repository).deleteById(existingId); //Quando deleteById(Id Existente) for chamado, não retornar nada

Mockito.doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(nonExistingId); //Quando deleteById(Id Inexistente) for chamado, lançar EmptyResultDataAccessException
```
- `when` - usado para definir o comportamento de um mock.
- `then` - usado para validar o resultado do mock.
- `thenReturn` - usado para retornar um valor quando o mock for chamado.
- `doNothing` - usado para não fazer nada quando o mock for chamado.
- `doThrow` - usado para lançar uma exceção quando o mock for chamado.

### Mockito e Mock - Service
```java
@Test
public void findAllShouldReturnPage() {
    Page<Product> page = new PageImpl<>(Collections.singletonList(createProduct()));
    when(repository.findAll((Pageable)ArgumentMatchers.any())).thenReturn(page);
    
    service.findAll();
}