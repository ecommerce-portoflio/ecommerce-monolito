-- V002 - Adiciona coluna ativo em produtos

alter table if exists produtos
    add column if not exists ativo boolean not null default true;
