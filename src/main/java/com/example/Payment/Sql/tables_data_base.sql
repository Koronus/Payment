-- Упрощенная структура БД с базовыми ограничениями

CREATE TABLE Users (
    User_ID SERIAL PRIMARY KEY,
    Surname VARCHAR(100) NOT NULL,
    Name_User VARCHAR(100) NOT NULL,
    Patronymic VARCHAR(100),
    Email VARCHAR(255) UNIQUE NOT NULL,
    Created_At TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Updated_At TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    -- Убраны сложные CHECK-ограничения - валидация в приложении
);

CREATE TABLE Operations (
    Operations_ID SERIAL PRIMARY KEY,
    amount DECIMAL(15,2) NOT NULL,
    purpose TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    User_ID INTEGER NOT NULL,
    
    FOREIGN KEY(User_ID) REFERENCES Users(User_ID) ON DELETE CASCADE,
    
    -- Оставлены только самые базовые проверки
    CONSTRAINT chk_positive_amount CHECK (amount > 0)
    -- Убрана проверка purpose - делается в приложении
);

CREATE TABLE Status (
    StatusID SERIAL PRIMARY KEY,
    Status_name VARCHAR(50) NOT NULL,
    Status_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    OperationID VARCHAR(100),
    Comment_operation TEXT,
    Operations_ID INTEGER NOT NULL,
    
    FOREIGN KEY(Operations_ID) REFERENCES Operations(Operations_ID) ON DELETE CASCADE
    
    -- Убраны все CHECK-ограничения - бизнес-логика в приложении
);

CREATE TABLE Banking_Details (
    Details_ID SERIAL PRIMARY KEY,
    Operations_ID INTEGER UNIQUE NOT NULL,
    Account_number VARCHAR(20) NOT NULL,
    Bank_bik VARCHAR(9) NOT NULL,
    recipient_phone VARCHAR(15),
    
    
    FOREIGN KEY(Operations_ID) REFERENCES Operations(Operations_ID) ON DELETE CASCADE
    
    -- Убраны все сложные CHECK-ограничения формата
    -- Оставлены только базовые ограничения NOT NULL, UNIQUE, FOREIGN KEY
);

-- Индексы остаются без изменений
CREATE INDEX idx_operations_user_id ON Operations(User_ID);
CREATE INDEX idx_operations_created_at ON Operations(created_at);
CREATE INDEX idx_status_operations_id ON Status(Operations_ID);
CREATE INDEX idx_status_date ON Status(Status_date);
CREATE INDEX idx_banking_details_operations_id ON Banking_Details(Operations_ID);
CREATE INDEX idx_users_email ON Users(Email);

