CREATE TABLE billing_accounts (
    id            VARCHAR(36)  NOT NULL,
    patient_id    VARCHAR(36)  NOT NULL,
    patient_name  VARCHAR(150) NOT NULL,
    patient_email VARCHAR(254) NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_billing_accounts PRIMARY KEY (id),
    CONSTRAINT uk_billing_accounts_patient_id UNIQUE (patient_id)
);
