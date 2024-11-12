
CREATE TABLE IF NOT EXISTS salaries (
    id SERIAL PRIMARY KEY,
    salary NUMERIC(19, 2),          -- Numeric type with precision for decimal values
    employee VARCHAR(255) NOT NULL, -- Employee name or identifier
    salary_date TIMESTAMP           -- Date and time of the salary
);