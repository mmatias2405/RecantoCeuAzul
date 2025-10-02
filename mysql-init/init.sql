
CREATE DATABASE IF NOT EXISTS recantoceuazul;
USE recantoceuazul;

CREATE TABLE IF NOT EXISTS ator (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100),
    telefone VARCHAR(15),
    nome VARCHAR(100),
    senha VARCHAR(255),
    papel VARCHAR (5)
);

CREATE TABLE IF NOT EXISTS medicao (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    volume_agua FLOAT,
    delta FLOAT,
    data_medicao DATETIME,
    fk_Residencia_id INTEGER,
    fk_Ator_id INTEGER
);

CREATE TABLE IF NOT EXISTS residencia (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    numero INTEGER
);

CREATE TABLE IF NOT EXISTS possuir (
    fk_Ator_id INTEGER,
    fk_Residencia_id INTEGER
);
 
ALTER TABLE medicao ADD CONSTRAINT FK_Medicao_2
    FOREIGN KEY (fk_Residencia_id)
    REFERENCES residencia (id)
    ON DELETE CASCADE;
 
ALTER TABLE medicao ADD CONSTRAINT FK_Medicao_3
    FOREIGN KEY (fk_Ator_id)
    REFERENCES ator (id)
    ON DELETE SET NULL;
 
ALTER TABLE possuir ADD CONSTRAINT FK_Possuir_1
    FOREIGN KEY (fk_Ator_id)
    REFERENCES ator (id)
    ON DELETE RESTRICT;
 
ALTER TABLE possuir ADD CONSTRAINT FK_Possuir_2
    FOREIGN KEY (fk_Residencia_id)
    REFERENCES residencia (id)
    ON DELETE SET NULL;

/* Populando o Banco de dados */
INSERT INTO ator (email,senha,nome,telefone,papel) VALUES ('admin@gmail.com','admin','Adm','(11)11111-1111','ADMIN');

-- É preciso mudar o delimitador para o MySQL entender o bloco inteiro
DELIMITER $$

CREATE PROCEDURE InserirResidencias()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 150 DO
        INSERT INTO residencia (numero) VALUES (i);
        SET i = i + 1;
    END WHILE;
END$$

-- Volta ao delimitador padrão
DELIMITER ;

-- Executa o procedimento que acabou de criar
CALL InserirResidencias();

-- (Opcional) Remove o procedimento após o uso
DROP PROCEDURE InserirResidencias;
