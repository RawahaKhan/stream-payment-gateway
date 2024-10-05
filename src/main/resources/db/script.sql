IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'payment_gateway')
BEGIN
    CREATE DATABASE payment_gateway;
    PRINT 'Database created successfully';
END
ELSE
BEGIN
    PRINT 'Database already exists';
END

GO
USE payment_gateway;

GO
-- Create payment schema if it doesn't exist
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'payment')
BEGIN
    EXEC('CREATE SCHEMA payment');
END;

GO

-- Create security schema if it doesn't exist
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'security')
BEGIN
    EXEC('CREATE SCHEMA security');
END;

GO

-- Create Users table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID('security.Users') AND type = 'U')
BEGIN
	CREATE TABLE security.Users (
		id INT PRIMARY KEY IDENTITY(1,1),
		username VARCHAR(50) UNIQUE NOT NULL,
		password VARCHAR(255) NOT NULL
	);
END
GO


-- Insert sample User (You can customize this as needed)
IF NOT EXISTS (SELECT * FROM security.Users WHERE username = 'username')
BEGIN
    INSERT INTO security.Users (username, password)
    VALUES 
    ('username', 'password');
END;

GO

-- Create the PaymentMethods table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'PaymentMethods' AND schema_id = SCHEMA_ID('payment'))
BEGIN
    CREATE TABLE payment.PaymentMethods
    (
        PaymentMethodID INT IDENTITY(1,1) PRIMARY KEY, -- Auto-increment primary key
        Name VARCHAR(100), -- Name of the payment method
        DisplayName VARCHAR(100), -- Display name of the payment method
        PaymentType VARCHAR(50), -- Type of payment (e.g., Credit Card, Mobile Carrier, etc.)
        Country VARCHAR(10), -- Country where this method is available
        CreatedAt DATETIME DEFAULT GETDATE(), -- Record creation timestamp
        UpdatedAt DATETIME DEFAULT GETDATE() -- Record last update timestamp
    );

    -- Step 2.1: Create indexes for better performance
    CREATE NONCLUSTERED INDEX IX_PaymentMethods_Country
    ON payment.PaymentMethods (Country);

    CREATE NONCLUSTERED INDEX IX_PaymentMethods_Name
    ON payment.PaymentMethods (Name);
END;

GO
-- Create the PaymentPlans table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'PaymentPlans' AND schema_id = SCHEMA_ID('payment'))
BEGIN
    CREATE TABLE payment.PaymentPlans
    (
        PaymentPlanID INT IDENTITY(1,1) PRIMARY KEY, -- Auto-increment primary key
        PaymentMethodID INT NOT NULL, -- Foreign key to PaymentMethods
        NetAmount DECIMAL(18, 2), -- Net price of the payment plan
        TaxAmount DECIMAL(18, 2), -- Tax amount
        GrossAmount DECIMAL(18, 2), -- Gross price of the payment plan
        Currency VARCHAR(10), -- Currency for the payment plan
        Duration VARCHAR(50), -- Duration for the payment plan (e.g., Month, Year, Week)
        CreatedAt DATETIME DEFAULT GETDATE(), -- Record creation timestamp
        UpdatedAt DATETIME DEFAULT GETDATE(), -- Record last update timestamp
        FOREIGN KEY (PaymentMethodID) REFERENCES payment.PaymentMethods(PaymentMethodID) ON DELETE CASCADE -- Foreign key relationship
    );

    -- Step 3.1: Create indexes for better performance
    CREATE NONCLUSTERED INDEX IX_PaymentPlans_PaymentMethodID
    ON payment.PaymentPlans (PaymentMethodID);
END;

GO
--  Add sample data to PaymentMethods and PaymentPlans

-- Insert sample Payment Methods (You can customize this as needed)
IF NOT EXISTS (SELECT * FROM payment.PaymentMethods WHERE Name = 'credit card')
BEGIN
    INSERT INTO payment.PaymentMethods (Name, DisplayName, PaymentType, Country)
    VALUES 
    ('credit card', 'Credit Card', 'CREDIT_CARD', 'US'),
    ('alfa_lb', 'Alfa Lebanon', 'MOBILE_CARRIER', 'SA'),
    ('voucher', 'Voucher', 'VOUCHER', 'AE');
END;

GO
-- Insert sample Payment Plans (You can customize this as needed)
IF NOT EXISTS (SELECT * FROM payment.PaymentPlans WHERE PaymentMethodID = 1)
BEGIN
    INSERT INTO payment.PaymentPlans (PaymentMethodID, NetAmount, TaxAmount, GrossAmount, Currency, Duration)
    VALUES
    (1, 5.99, 0, 5.99, 'USD', 'Month'),
    (2, 5.99, 0, 5.99, 'USD', 'Month'),
    (2, 10.00, 0, 10.00, 'SAR', 'Week'),
    (3, 0, 0, 0, 'AED', NULL);
END;

GO

/****** Creating Type [PAT].[NonAttributedPatientType] Script Date: 23/09/2024 ******/
IF NOT EXISTS (SELECT * FROM sys.types WHERE is_table_type = 1 AND name = 'PaymentPlanTableType')
BEGIN
    CREATE TYPE [payment].[PaymentPlanTableType] AS TABLE
(
    PaymentPlanID INT,           -- If the plan exists, pass its ID; otherwise, pass NULL for a new plan
    NetAmount DECIMAL(10,2),
    TaxAmount DECIMAL(10,2),
    GrossAmount DECIMAL(10,2),
    Currency VARCHAR(10),
    Duration VARCHAR(50)
);

END

GO


IF EXISTS (
    SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[payment].[USP_UPDATE_PAYMENT_METHOD]')
)
BEGIN
    DROP PROCEDURE [payment].[USP_UPDATE_PAYMENT_METHOD]
END
GO

CREATE PROCEDURE [payment].[USP_UPDATE_PAYMENT_METHOD]
(
    @p_PaymentMethodID INT,
    @p_Name VARCHAR(255),
    @p_DisplayName VARCHAR(255),
    @p_PaymentType VARCHAR(50),
    @p_Country VARCHAR(50),
    @PaymentPlans [payment].[PaymentPlanTableType] READONLY
)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @NewPaymentMethodID INT;

    BEGIN TRY
        -- Check if the PaymentMethodID exists
        IF EXISTS (SELECT 1 FROM payment.PaymentMethods WHERE PaymentMethodID = @p_PaymentMethodID)
        BEGIN
            -- If it exists, perform the update
            UPDATE payment.PaymentMethods
            SET Name = @p_Name,
                DisplayName = @p_DisplayName,
                PaymentType = @p_PaymentType,
                Country = @p_Country
            WHERE PaymentMethodID = @p_PaymentMethodID;

            -- Set the PaymentMethodID for further use
            SET @NewPaymentMethodID = @p_PaymentMethodID;
        END
        ELSE
        BEGIN
			/*
            -- If it does not exist, perform the insert
            INSERT INTO payment.PaymentMethods (Name, DisplayName, PaymentType, Country)
            VALUES (@p_Name, @p_DisplayName, @p_PaymentType, @p_Country);

            -- Get the newly inserted PaymentMethodID
            SET @NewPaymentMethodID = SCOPE_IDENTITY();
			*/
			-- Set the PaymentMethodID to -1
            SET @NewPaymentMethodID = -1;
			 -- Return immediately after setting the PaymentMethodID to -1
            SELECT @NewPaymentMethodID AS PaymentMethodID;
            RETURN;  -- Exit the procedure
        END

        -- Update or Insert Payment Plans using the PaymentMethodID
        MERGE INTO payment.PaymentPlans AS target
        USING @PaymentPlans AS source
        ON target.PaymentPlanID = source.PaymentPlanID
        AND target.PaymentMethodID = @NewPaymentMethodID
        WHEN MATCHED THEN
            UPDATE SET target.NetAmount = source.NetAmount,
                       target.TaxAmount = source.TaxAmount,
                       target.GrossAmount = source.GrossAmount,
                       target.Currency = source.Currency,
                       target.Duration = source.Duration
		
        WHEN NOT MATCHED BY TARGET THEN
            INSERT (PaymentMethodID, NetAmount, TaxAmount, GrossAmount, Currency, Duration)
            VALUES (@NewPaymentMethodID, source.NetAmount, source.TaxAmount, source.GrossAmount, source.Currency, source.Duration);
		
			
		 -- Return only the Payment Method ID
        SELECT @NewPaymentMethodID AS PaymentMethodID;

    END TRY
    BEGIN CATCH
        -- Error handling
        THROW;
    END CATCH
END;




