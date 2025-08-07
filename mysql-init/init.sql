
CREATE DATABASE IF NOT EXISTS recantoceuazul;
USE recantoceuazul;

CREATE TABLE IF NOT EXISTS administrador (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100),        
    telefone VARCHAR(15),       
    nome VARCHAR(100),        
    senha VARCHAR(255)          
);

CREATE TABLE IF NOT EXISTS abastecimento (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    volume_agua FLOAT,
    hora_inicio DATETIME,
    hora_fim DATETIME,
    fk_Setor_id INTEGER,
    fk_Administrador_id INTEGER
);

CREATE TABLE IF NOT EXISTS setor (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS captacao (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    volume_agua FLOAT,
    hora DATETIME,
    fk_Administrador_id INTEGER
);

ALTER TABLE abastecimento ADD CONSTRAINT FK_Abastecimento_2
    FOREIGN KEY (fk_Setor_id)
    REFERENCES setor (id)
    ON DELETE CASCADE;
 
ALTER TABLE abastecimento ADD CONSTRAINT FK_Abastecimento_3
    FOREIGN KEY (fk_Administrador_id)
    REFERENCES administrador (id)
    ON DELETE CASCADE;
 
ALTER TABLE captacao ADD CONSTRAINT FK_Captacao_2
    FOREIGN KEY (fk_Administrador_id)
    REFERENCES administrador (id)
    ON DELETE CASCADE;

/* Populando o Banco de dados */
INSERT INTO administrador (email,senha,nome,telefone) VALUES ('admin@gmail.com','admin','Adm','(11)11111-1111');
INSERT INTO setor (nome) VALUES ('SETOR 1');
INSERT INTO setor (nome) VALUES ('SETOR B');
INSERT INTO setor (nome) VALUES ('SETOR 3');
INSERT INTO setor (nome) VALUES ('SETOR 4');
INSERT INTO setor (nome) VALUES ('SETOR E');