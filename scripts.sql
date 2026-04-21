/* =============================================================
SCRIPT DE CRÉATION DE LA BASE DE DONNÉES BOOKHUB (MPD)
Cible : SQL Server 2019+
============================================================= */
-- 1. CRÉATION DE LA BASE DE DONNÉES
USE master;

GO IF NOT EXISTS (
    SELECT
        *
    FROM
        sys.databases
    WHERE
        name = 'BookHub'
) BEGIN CREATE DATABASE BookHub;

END GO USE BookHub;

GO
-- 2. SUPPRESSION DES TABLES (DANS L'ORDRE INVERSE DES FK) POUR RÉINITIALISATION SI BESOIN
IF OBJECT_ID ('Notations', 'U') IS NOT NULL
DROP TABLE Notations;

IF OBJECT_ID ('Reservations', 'U') IS NOT NULL
DROP TABLE Reservations;

IF OBJECT_ID ('Emprunts', 'U') IS NOT NULL
DROP TABLE Emprunts;

IF OBJECT_ID ('Livres', 'U') IS NOT NULL
DROP TABLE Livres;

IF OBJECT_ID ('Categories', 'U') IS NOT NULL
DROP TABLE Categories;

IF OBJECT_ID ('Utilisateurs', 'U') IS NOT NULL
DROP TABLE Utilisateurs;

GO
-- 3. CRÉATION DES TABLES INDÉPENDANTES
CREATE TABLE
    Categories (
        id INT IDENTITY (1, 1) PRIMARY KEY,
        nom NVARCHAR (50) NOT NULL
    );

CREATE TABLE
    Utilisateurs (
        id INT IDENTITY (1, 1) PRIMARY KEY,
        nom NVARCHAR (50) NOT NULL,
        prenom NVARCHAR (50) NOT NULL,
        email NVARCHAR (100) NOT NULL UNIQUE,
        telephone NVARCHAR (20) NOT NULL UNIQUE,
        mot_de_passe NVARCHAR (255) NOT NULL,
        role NVARCHAR (20) NOT NULL CHECK (role IN ('UTILISATEUR', 'ADMIN', 'LIBRAIRE')),
        date_creation DATETIME DEFAULT GETDATE ()
    );

-- 4. CRÉATION DE LA TABLE LIVRES
CREATE TABLE
    Livres (
        id INT IDENTITY (1, 1) PRIMARY KEY,
        titre NVARCHAR (255) NOT NULL,
        auteur NVARCHAR (255) NOT NULL,
        isbn NVARCHAR (20) NOT NULL UNIQUE,
        date_parution DATE NOT NULL,
        nombre_pages INT NOT NULL,
        description NVARCHAR (MAX) NOT NULL,
        url_couverture NVARCHAR (255),
        total_exemplaires INT DEFAULT 1 CHECK (total_exemplaires >= 0),
        exemplaires_disponibles INT DEFAULT 1 CHECK (exemplaires_disponibles >= 0),
        categorie_id INT NOT NULL,
        CONSTRAINT FK_Livres_Categories FOREIGN KEY (categorie_id) REFERENCES Categories (id)
    );

-- 5. CRÉATION DES TABLES DE MOUVEMENTS
CREATE TABLE
    Emprunts (
        id INT IDENTITY (1, 1) PRIMARY KEY,
        utilisateur_id INT NOT NULL,
        livre_id INT NOT NULL,
        date_emprunt DATETIME DEFAULT GETDATE (),
        date_retour_prevue DATETIME NOT NULL,
        date_retour_effective DATETIME NULL,
        statut NVARCHAR (20) DEFAULT 'EN COURS' CHECK (statut IN ('EN COURS', 'RENDU', 'EN RETARD')),
        CONSTRAINT FK_Emprunts_Utilisateurs FOREIGN KEY (utilisateur_id) REFERENCES Utilisateurs (id),
        CONSTRAINT FK_Emprunts_Livres FOREIGN KEY (livre_id) REFERENCES Livres (id)
    );

CREATE TABLE
    Reservations (
        id INT IDENTITY (1, 1) PRIMARY KEY,
        utilisateur_id INT NOT NULL,
        livre_id INT NOT NULL,
        date_reservation DATETIME DEFAULT GETDATE (),
        rang_file_attente INT NOT NULL,
        statut NVARCHAR (20) DEFAULT 'EN_ATTENTE' CHECK (statut IN ('EN_ATTENTE', 'DISPONIBLE', 'ANNULEE')),
        CONSTRAINT FK_Reservations_Utilisateurs FOREIGN KEY (utilisateur_id) REFERENCES Utilisateurs (id),
        CONSTRAINT FK_Reservations_Livres FOREIGN KEY (livre_id) REFERENCES Livres (id)
    );

CREATE TABLE
    Notations (
        id INT IDENTITY (1, 1) PRIMARY KEY,
        utilisateur_id INT NOT NULL,
        livre_id INT NOT NULL,
        evaluation INT NOT NULL CHECK (evaluation BETWEEN 1 AND 5),
        CONSTRAINT FK_Notations_Utilisateurs FOREIGN KEY (utilisateur_id) REFERENCES Utilisateurs (id),
        CONSTRAINT FK_Notations_Livres FOREIGN KEY (livre_id) REFERENCES Livres (id)
    );

GO
-- 6. TRIGGERS DE GESTION AUTOMATIQUE DES STOCKS
CREATE TRIGGER trg_AfterInsert_Emprunt ON Emprunts AFTER INSERT AS BEGIN
SET
    NOCOUNT ON;

UPDATE Livres
SET
    exemplaires_disponibles = exemplaires_disponibles - 1
FROM
    Livres
    INNER JOIN inserted ON Livres.id = inserted.livre_id;

END;

GO CREATE TRIGGER trg_AfterUpdate_Emprunt_Retour ON Emprunts AFTER
UPDATE AS BEGIN
SET
    NOCOUNT ON;

IF
UPDATE (statut) BEGIN
UPDATE Livres
SET
    exemplaires_disponibles = exemplaires_disponibles + 1
FROM
    Livres
    INNER JOIN inserted i ON Livres.id = i.livre_id
    INNER JOIN deleted d ON i.id = d.id
WHERE
    i.statut = 'RENDU'
    AND d.statut <> 'RENDU';

END;

END;

GO