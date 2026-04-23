/* =============================================================
SCRIPT COMPLET : BASE DE DONNÉES BOOKHUB
Cible : SQL Server 2019+
Inclus : MPD, Triggers et Jeu de données de test complet
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
-- 2. RÉINITIALISATION DES TABLES (Ordre respectant les FK)
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
                   nom NVARCHAR (50) NOT NULL UNIQUE
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
-- 6. TRIGGERS DE GESTION DES STOCKS
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
-- 7. JEU DE DONNÉES DE TEST (INSERTIONS)
-- 7.1 Catégories
INSERT INTO
    Categories (nom)
VALUES
    ('Science-Fiction'),
    ('Fantastique'),
    ('Thriller'),
    ('Développement Personnel'),
    ('Informatique');

-- 7.2 Utilisateurs
INSERT INTO
    Utilisateurs (nom, prenom, email, telephone, mot_de_passe, role)
VALUES
    (
        'Dupont',
        'Jean',
        'jean.dupont@email.com',
        '0601020304',
        'hash_pw_1',
        'UTILISATEUR'
    ),
    (
        'Martin',
        'Sophie',
        'sophie.martin@email.com',
        '0611121314',
        'hash_pw_2',
        'LIBRAIRE'
    ),
    (
        'Admin',
        'Global',
        'admin@bookhub.com',
        '0621222324',
        'hash_pw_3',
        'ADMIN'
    ),
    (
        'Lefebvre',
        'Thomas',
        'thomas.l@email.com',
        '0650403020',
        'hash_pw_4',
        'UTILISATEUR'
    ),
    (
        'Moreau',
        'Camille',
        'c.moreau@email.com',
        '0780901020',
        'hash_pw_5',
        'UTILISATEUR'
    );

-- 7.3 Livres
INSERT INTO
    Livres (
    titre,
    auteur,
    isbn,
    date_parution,
    nombre_pages,
    description,
    total_exemplaires,
    exemplaires_disponibles,
    categorie_id
)
VALUES
    (
        'Dune',
        'Frank Herbert',
        '9782221241424',
        '1965-08-01',
        712,
        'L''épopée de Paul Atréides sur Arrakis.',
        3,
        3,
        1
    ),
    (
        'Le Hobbit',
        'J.R.R. Tolkien',
        '9782253049470',
        '1937-09-21',
        320,
        'Un voyage inattendu pour Bilbon Sacquet.',
        2,
        2,
        2
    ),
    (
        'Clean Code',
        'Robert C. Martin',
        '9780132350884',
        '2008-08-01',
        464,
        'Manuel de savoir-vivre du développeur.',
        5,
        5,
        5
    ),
    (
        'Le Silence des Agneaux',
        'Thomas Harris',
        '9782266208945',
        '1988-05-19',
        400,
        'Un thriller psychologique intense.',
        2,
        2,
        3
    ),
    (
        '1984',
        'George Orwell',
        '9782070409495',
        '1949-06-08',
        376,
        'Big Brother vous regarde.',
        4,
        4,
        1
    ),
    (
        'Atomic Habits',
        'James Clear',
        '9781847941831',
        '2018-10-16',
        320,
        'Changer ses habitudes avec de petits pas.',
        3,
        3,
        4
    ),
    (
        'Brave New World Revisited',
        'Aldous Huxley',
        '9780060898526',
        '1958-01-01',
        144,
        'Essai sur les dangers des sociétés modernes.',
        6,
        6,
        1
    ),
    (
        'Fahrenheit 451',
        'Ray Bradbury',
        '9781451673319',
        '1953-10-19',
        249,
        'Une société où les livres sont interdits.',
        7,
        7,
        1
    ),
    (
        'La Servante écarlate',
        'Margaret Atwood',
        '9782221196984',
        '1985-01-01',
        416,
        'Une dystopie sur une société totalitaire religieuse.',
        5,
        5,
        1
    ),
    (
        'Neuromancien',
        'William Gibson',
        '9780441569595',
        '1984-07-01',
        271,
        'Un classique du cyberpunk.',
        6,
        6,
        2
    ),
    (
        'Fondation',
        'Isaac Asimov',
        '9780553293357',
        '1951-01-01',
        255,
        'La chute et renaissance d’un empire galactique.',
        8,
        8,
        2
    ),
    (
        'Snow Crash',
        'Neal Stephenson',
        '9780553380958',
        '1992-06-01',
        480,
        'Un roman culte du cyberpunk et du métavers.',
        4,
        4,
        2
    ),
    (
        'L''Étranger',
        'Albert Camus',
        '9782070360024',
        '1942-05-19',
        186,
        'Un homme face à l’absurdité de la vie.',
        6,
        6,
        3
    ),
    (
        'La Peste',
        'Albert Camus',
        '9782070360420',
        '1947-06-10',
        308,
        'Une épidémie révélant la nature humaine.',
        5,
        5,
        3
    ),
    (
        'Les Misérables',
        'Victor Hugo',
        '9782253096344',
        '1862-01-01',
        1463,
        'Une fresque sociale et humaine.',
        3,
        3,
        3
    ),
    (
        'Le Comte de Monte-Cristo',
        'Alexandre Dumas',
        '9782070409181',
        '1844-01-01',
        1243,
        'Une vengeance magistrale.',
        4,
        4,
        3
    ),
    (
        'Sapiens',
        'Yuval Noah Harari',
        '9780062316097',
        '2011-01-01',
        443,
        'Histoire de l’humanité.',
        9,
        9,
        4
    ),
    (
        'Homo Deus',
        'Yuval Noah Harari',
        '9780062464316',
        '2015-01-01',
        450,
        'L’avenir de l’humanité.',
        7,
        7,
        4
    ),
    (
        'Deep Work',
        'Cal Newport',
        '9781455586691',
        '2016-01-05',
        296,
        'Se concentrer dans un monde de distractions.',
        5,
        5,
        4
    ),
    (
        'Thinking, Fast and Slow',
        'Daniel Kahneman',
        '9780374533557',
        '2011-10-25',
        499,
        'Deux systèmes de pensée.',
        6,
        6,
        4
    ),
    (
        'Zero to One',
        'Peter Thiel',
        '9780804139298',
        '2014-09-16',
        224,
        'Créer de l’innovation.',
        4,
        4,
        4
    ),
    (
        'The Pragmatic Programmer',
        'Andrew Hunt',
        '9780201616224',
        '1999-10-20',
        352,
        'Bonnes pratiques du développeur.',
        5,
        5,
        5
    ),
    (
        'Refactoring',
        'Martin Fowler',
        '9780201485677',
        '1999-07-08',
        431,
        'Améliorer du code existant.',
        3,
        3,
        5
    ),
    (
        'Design Patterns',
        'Erich Gamma',
        '9780201633610',
        '1994-10-21',
        395,
        'Solutions de conception logicielle.',
        4,
        4,
        5
    ),
    (
        'Head First Design Patterns',
        'Eric Freeman',
        '9780596007126',
        '2004-10-01',
        694,
        'Apprendre les patterns facilement.',
        6,
        6,
        5
    ),
    (
        'Eloquent JavaScript',
        'Marijn Haverbeke',
        '9781593279509',
        '2018-12-04',
        472,
        'Guide moderne JavaScript.',
        7,
        7,
        5
    );

-- 7.4 Emprunts (Le trigger va décrémenter le stock)
-- Emprunts en cours
INSERT INTO
    Emprunts (
    utilisateur_id,
    livre_id,
    date_emprunt,
    date_retour_prevue,
    statut
)
VALUES
    (
        1,
        1,
        GETDATE (),
        DATEADD (day, 14, GETDATE ()),
        'EN COURS'
    ),
    (
        1,
        3,
        GETDATE (),
        DATEADD (day, 14, GETDATE ()),
        'EN COURS'
    ),
    (
        4,
        5,
        GETDATE (),
        DATEADD (day, 14, GETDATE ()),
        'EN COURS'
    );

-- Emprunt en retard (simulé)
INSERT INTO
    Emprunts (
    utilisateur_id,
    livre_id,
    date_emprunt,
    date_retour_prevue,
    statut
)
VALUES
    (
        5,
        6,
        DATEADD (day, -20, GETDATE ()),
        DATEADD (day, -6, GETDATE ()),
        'EN RETARD'
    );

-- Emprunt déjà rendu (simulé)
INSERT INTO
    Emprunts (
    utilisateur_id,
    livre_id,
    date_emprunt,
    date_retour_prevue,
    date_retour_effective,
    statut
)
VALUES
    (
        4,
        2,
        DATEADD (day, -30, GETDATE ()),
        DATEADD (day, -16, GETDATE ()),
        DATEADD (day, -17, GETDATE ()),
        'RENDU'
    );

-- 7.5 Réservations
INSERT INTO
    Reservations (
    utilisateur_id,
    livre_id,
    rang_file_attente,
    statut
)
VALUES
    (5, 1, 1, 'EN_ATTENTE'),
    (4, 1, 2, 'EN_ATTENTE');

-- 7.6 Notations
INSERT INTO
    Notations (utilisateur_id, livre_id, evaluation)
VALUES
    (1, 1, 5),
    (1, 3, 4),
    (4, 2, 5);

GO
-- 8. VÉRIFICATION FINALE
SELECT
    titre,
    total_exemplaires,
    exemplaires_disponibles
FROM
    Livres;

SELECT
    *
FROM
    Emprunts
WHERE
    statut = 'EN RETARD';