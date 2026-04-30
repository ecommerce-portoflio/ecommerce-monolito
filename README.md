# 🛒 E-commerce Monolito API
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3-green)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue)
![Build](https://img.shields.io/github/actions/workflow/status/ecommerce-portoflio/ecommerce-monolito/testes.yaml)

API backend para um e-commerce monolítico, construída com foco em arquitetura limpa, segurança com JWT, testes de integração com Testcontainers e deploy containerizado com Docker.

## 🎯 Objetivo

Este projeto foi desenvolvido com o objetivo de consolidar conhecimentos em desenvolvimento backend com Spring Boot, abordando autenticação segura, testes de integração e práticas de deploy com Docker.

## 🚀 Funcionalidades

* 👤 **Gerenciamento de Usuários**: Cadastro, login e atualização de perfil.
* 🔐 **Autenticação e Autorização**: Endpoints seguros utilizando Spring Security e JSON Web Tokens (JWT). Controle de acesso baseado em papéis com hierarquia (ADMIN > MODERADOR > CLIENTE).
* 📦 **Gerenciamento de Produtos**: Operações completas de CRUD (Criar, Ler, Atualizar, Deletar) para produtos. Produtos podem ser desativados e reativados.
* ⭐ **Avaliações de Produtos**: Usuários podem adicionar, atualizar e remover avaliações. A média das avaliações é calculada e armazenada automaticamente.
* 🛒 **Carrinho de Compras**: Adicionar produtos ao carrinho, visualizar, remover itens individuais e limpar o carrinho completo.
* 📑 **Processamento de Pedidos**:
  * Criar pedidos a partir de um único produto, múltiplos produtos ou todo o carrinho.
  * Simular pagamento e acompanhar status de entrega.
  * Usuários podem visualizar histórico de compras.
  * Vendedores podem visualizar histórico de vendas.
* 📧 **Notificações por Email**: Envio assíncrono de emails para eventos importantes como criação de pedido, confirmação de pagamento e entrega.
* 🗄️ **Migrações de Banco de Dados**: Uso do Flyway para controle de versionamento do schema.
* 🐳 **Containerização**: Docker e Docker Compose para facilitar execução e deploy.

---

## 🧰 Tecnologias Utilizadas

* **Backend**: Java 21, Spring Boot 3  
* **Banco de Dados**: PostgreSQL  
* **ORM e Migrações**: Spring Data JPA, Flyway  
* **Segurança**: Spring Security, JWT (java-jwt)  
* **Build**: Maven  
* **Testes**: JUnit 5, REST Assured, Testcontainers  
* **Containerização**: Docker, Docker Compose  
* **Email**: Spring Boot Starter Mail  
* **Utilitários**: Lombok  

---

## ⚙️ Pré-requisitos

Antes de começar, certifique-se de ter instalado:

* Java JDK 21 ou superior  
* Maven 3.9 ou superior  
* Docker e Docker Compose  

---

## ▶️ Como Executar

### 1. Clonar o repositório

```bash
git clone https://github.com/ecommerce-portoflio/ecommerce-monolito.git
cd ecommerce-monolito
```


### 2. Variáveis de ambiente

A aplicação utiliza variáveis de ambiente. Crie dois arquivos (`.env` e `db.env`) a partir do exemplo:

```bash
cp .env.example .env
cp .env.example db.env
```

Agora, edite os arquivos com os valores adequados

**Arquivo: `db.env`** (container PostgreSQL)
```properties
POSTGRES_USER=myuser
POSTGRES_PASSWORD=mypassword
POSTGRES_DB=ecommerce
```

**Arquivo: `.env`** (container da aplicação)
```properties
DB_USERNAME=myuser
DB_PASSWORD=mypassword
DB_URL=jdbc:postgresql://postgres-ecommerce:5432/ecommerce
SECRET=your-very-secret-jwt-key
MAIL_USER=your.email@gmail.com
MAIL_PASSWORD=your-gmail-app-password
```
>**Observação:** O `DB_URL` em `.env` usa `postgres-ecommerce` porque a aplicação roda dentro da rede Docker. Se rodar fora do Docker, altere para `localhost`.

### 3. Executando a aplicação
```bash
docker-compose up --build
```

Esse comando irá:
1.  Construir a imagem Docker da aplicação.
2.  Subir três containers:
    *   `postgres-ecommerce`: O Banco de Dados PostgreSQL.
    *   `pgadmin-ecommerce`: interface de gerenciamento (disponível em `http://localhost:5050`).
    *   `API_ecommerce`: A aplicação Spring Boot.

A API estará disponível em: `http://localhost:8080`.

---

## 🔗 Endpoints da API

A API disponibiliza os seguintes recursos:

* `/auth` → autenticação e geração de token
* `/usuario` → gerenciamento de usuários
* `/produto` → catálogo e CRUD de produtos
* `/carrinho` → gerenciamento do carrinho
* `/pedido` → criação e processamento de pedidos
* `/avaliacao` → avaliações de produtos

---

## 🏗️ Arquitetura

- API REST monolítica
- Banco relacional PostgreSQL
- Autenticação stateless com JWT
- Execução via containers Docker

---

## 🔄 Fluxo de Uso da API

Exemplo de fluxo completo: login → adicionar produto ao carrinho → finalizar pedido.

### 1. Login
```http
POST http://localhost:8080/auth/login
Body:
    {
      "email": "seuemail@email.com",
      "senha": "Senha@12345"
    }
```
Resposta: token-jwt (usá-lo nas próximas requisições)

### 2. Adicionar produto ao carrinho
```http
POST http://localhost:8080/carrinho
Headers:
    Authorization: Bearer jwt-token-aqui
Body:
    {
      "idProduto": 2,
      "quantidade": 1
    }
```
Resposta: "Produto adicionado ao carrinho!"

### 3. Finalizar pedido a partir do carrinho
```http
POST http://localhost:8080/pedido/carrinho
Headers:
    Authorization: Bearer jwt-token-aqui
```
Resposta:
```json
{
  "id": 1,
  "valorTotal": 199.90,
  "dataCompra": null,
  "statusPedido": "AGUARDANDO_PAGAMENTO",
  "compradorId": 10,
  "produtos": [
    {
      "produtoId": 2,
      "quantidade": 1,
      "pedidoId": 1
    }
  ]
}
```
> * `dataCompra` será preenchida após a confirmação do pagamento.
>* O pedido é criado com status inicial `AGUARDANDO_PAGAMENTO`.
---

## 🧪 Testes

O projeto possui testes unitários e de integração.

Os testes de integração utilizam Testcontainers para subir um PostgreSQL real, garantindo isolamento e consistência.

Para executar:

```bash
./mvnw test
```
>**Observação:** Caso esteja rodando localmente e não tenha as variáveis de ambiente disponíveis, execute o comando com a flag `-Dvariável=valor` para cada variável de ambiente necessária

---

## 🔄 CI/CD

O repositório utiliza GitHub Actions:

* **`testes.yaml`**
    * Executado a cada Pull Request
    * Sobe um PostgreSQL
    * Configura variáveis de ambiente
    * Executa `mvn test`
* **`cd.yaml`**
    * Executado a cada push na branch master
    * Login no Docker Hub
    * Build da imagem
    * Push com tags:
        * latest
        * SHA do commit

---

## 🐳 Docker

Imagem disponível no Docker Hub:
https://hub.docker.com/repository/docker/marcelobezz07/ecommerce

Ou execute o comando:
```bash
docker pull marcelobezz07/ecommerce:latest
```
