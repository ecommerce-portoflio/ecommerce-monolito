-- V001 - Cria tabelas iniciais

create table if not exists usuarios (
    id bigserial primary key,
    nome varchar(255),
    email varchar(255) unique,
    senha varchar(255),
    telefone varchar(50),
    role varchar(50) not null,
    cpf varchar(20),
    cnpj varchar(20),
    ativo boolean not null default true
);

create table if not exists produtos (
    id bigserial primary key,
    nome varchar(255),
    descricao text,
    quantidade_estoque integer not null default 0,
    valor numeric(19, 2),
    media_avaliacoes double precision,
    vendedor_id bigint not null,
    constraint fk_produtos_vendedor foreign key (vendedor_id) references usuarios (id)
);

create index if not exists idx_produtos_vendedor_id on produtos (vendedor_id);

create table if not exists avaliacoes (
    id bigserial primary key,
    nota double precision,
    avaliador_id bigint,
    produto_id bigint,
    constraint fk_avaliacoes_avaliador foreign key (avaliador_id) references usuarios (id),
    constraint fk_avaliacoes_produto foreign key (produto_id) references produtos (id)
);

create index if not exists idx_avaliacoes_avaliador_id on avaliacoes (avaliador_id);
create index if not exists idx_avaliacoes_produto_id on avaliacoes (produto_id);

create table if not exists carrinhos (
    id bigserial primary key,
    total numeric(19, 2),
    usuario_id bigint not null unique,
    constraint fk_carrinhos_usuario foreign key (usuario_id) references usuarios (id)
);

create index if not exists idx_carrinhos_usuario_id on carrinhos (usuario_id);

create table if not exists carrinho_produtos (
    id bigserial primary key,
    quantidade integer,
    valor_unitario numeric(19, 2),
    produto_id bigint not null,
    carrinho_id bigint not null,
    constraint fk_carrinho_produtos_produto foreign key (produto_id) references produtos (id),
    constraint fk_carrinho_produtos_carrinho foreign key (carrinho_id) references carrinhos (id)
);

create index if not exists idx_carrinho_produtos_produto_id on carrinho_produtos (produto_id);
create index if not exists idx_carrinho_produtos_carrinho_id on carrinho_produtos (carrinho_id);

create table if not exists pedidos (
    id bigserial primary key,
    valor_total numeric(19, 2),
    data_compra timestamp,
    status_pedido varchar(50),
    comprador_id bigint not null,
    vendedor_id bigint not null,
    constraint fk_pedidos_comprador foreign key (comprador_id) references usuarios (id),
    constraint fk_pedidos_vendedor foreign key (vendedor_id) references usuarios (id),
    constraint chk_pedidos_status_pedido check (status_pedido in ('EM_ANDAMENTO','AGUARDANDO_PAGAMENTO','PAGO','ENTREGUE'))
);

create index if not exists idx_pedidos_comprador_id on pedidos (comprador_id);
create index if not exists idx_pedidos_vendedor_id on pedidos (vendedor_id);

create table if not exists pedido_produtos (
    id bigserial primary key,
    quantidade integer,
    pedido_id bigint not null,
    produto_id bigint not null,
    constraint fk_pedido_produtos_pedido foreign key (pedido_id) references pedidos (id),
    constraint fk_pedido_produtos_produto foreign key (produto_id) references produtos (id)
);

create index if not exists idx_pedido_produtos_pedido_id on pedido_produtos (pedido_id);
create index if not exists idx_pedido_produtos_produto_id on pedido_produtos (produto_id);

