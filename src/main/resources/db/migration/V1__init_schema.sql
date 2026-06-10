CREATE TABLE hotels (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        short_name VARCHAR(120),
                        address VARCHAR(500) NOT NULL,
                        phone VARCHAR(50),
                        email VARCHAR(255),
                        timezone VARCHAR(64) NOT NULL DEFAULT 'Europe/Moscow',
                        status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                        created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT chk_hotels_status
                            CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE room_types (
                            id BIGSERIAL PRIMARY KEY,
                            hotel_id BIGINT NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
                            code VARCHAR(50) NOT NULL,
                            name VARCHAR(120) NOT NULL,
                            description TEXT,
                            capacity INTEGER NOT NULL,
                            base_price NUMERIC(12, 2) NOT NULL,
                            status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                            created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT chk_room_types_capacity CHECK (capacity > 0),
                            CONSTRAINT chk_room_types_base_price CHECK (base_price >= 0),
                            CONSTRAINT chk_room_types_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
                            CONSTRAINT uk_room_types_hotel_code UNIQUE (hotel_id, code)
);

CREATE TABLE rooms (
                       id BIGSERIAL PRIMARY KEY,
                       hotel_id BIGINT NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
                       room_type_id BIGINT NOT NULL REFERENCES room_types(id),
                       room_number VARCHAR(50) NOT NULL,
                       floor INTEGER,
                       status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
                       housekeeping_status VARCHAR(30) NOT NULL DEFAULT 'CLEAN',
                       comment TEXT,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT chk_rooms_status
                           CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'MAINTENANCE', 'OUT_OF_SERVICE')),

                       CONSTRAINT chk_rooms_housekeeping_status
                           CHECK (housekeeping_status IN ('CLEAN', 'DIRTY', 'INSPECTED')),

                       CONSTRAINT uk_rooms_hotel_room_number
                           UNIQUE (hotel_id, room_number)
);

CREATE TABLE guests (
                        id BIGSERIAL PRIMARY KEY,
                        last_name VARCHAR(100) NOT NULL,
                        first_name VARCHAR(100) NOT NULL,
                        middle_name VARCHAR(100),
                        birth_date DATE,
                        phone VARCHAR(50),
                        email VARCHAR(255),
                        document_type VARCHAR(50),
                        document_series VARCHAR(50),
                        document_number VARCHAR(100),
                        citizenship VARCHAR(100),
                        comment TEXT,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE rate_plans (
                            id BIGSERIAL PRIMARY KEY,
                            hotel_id BIGINT NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
                            code VARCHAR(50) NOT NULL,
                            name VARCHAR(120) NOT NULL,
                            description TEXT,
                            meal_plan VARCHAR(50) NOT NULL DEFAULT 'NONE',
                            refundable BOOLEAN NOT NULL DEFAULT TRUE,
                            cancellation_policy TEXT,
                            status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                            created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT chk_rate_plans_meal_plan
                                CHECK (meal_plan IN ('NONE', 'BREAKFAST', 'HALF_BOARD', 'FULL_BOARD', 'ALL_INCLUSIVE')),

                            CONSTRAINT chk_rate_plans_status
                                CHECK (status IN ('ACTIVE', 'INACTIVE')),

                            CONSTRAINT uk_rate_plans_hotel_code
                                UNIQUE (hotel_id, code)
);

CREATE TABLE room_type_rates (
                                 id BIGSERIAL PRIMARY KEY,
                                 room_type_id BIGINT NOT NULL REFERENCES room_types(id) ON DELETE CASCADE,
                                 rate_plan_id BIGINT NOT NULL REFERENCES rate_plans(id) ON DELETE CASCADE,
                                 valid_from DATE NOT NULL,
                                 valid_to DATE NOT NULL,
                                 price NUMERIC(12, 2) NOT NULL,
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT chk_room_type_rates_dates CHECK (valid_to >= valid_from),
                                 CONSTRAINT chk_room_type_rates_price CHECK (price >= 0),
                                 CONSTRAINT uk_room_type_rates_period UNIQUE (room_type_id, rate_plan_id, valid_from, valid_to)
);

CREATE TABLE reservations (
                              id BIGSERIAL PRIMARY KEY,
                              hotel_id BIGINT NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
                              guest_id BIGINT NOT NULL REFERENCES guests(id),
                              reservation_number VARCHAR(50) NOT NULL,
                              source VARCHAR(50) NOT NULL DEFAULT 'DIRECT',
                              status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
                              check_in_date DATE NOT NULL,
                              check_out_date DATE NOT NULL,
                              adults INTEGER NOT NULL DEFAULT 1,
                              children INTEGER NOT NULL DEFAULT 0,
                              total_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
                              comment TEXT,
                              created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT chk_reservations_dates CHECK (check_out_date > check_in_date),
                              CONSTRAINT chk_reservations_adults CHECK (adults > 0),
                              CONSTRAINT chk_reservations_children CHECK (children >= 0),
                              CONSTRAINT chk_reservations_total_amount CHECK (total_amount >= 0),

                              CONSTRAINT chk_reservations_source
                                  CHECK (source IN ('DIRECT', 'PHONE', 'WEBSITE', 'AGENCY', 'BOOKING_PLATFORM')),

                              CONSTRAINT chk_reservations_status
                                  CHECK (status IN ('CREATED', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED', 'NO_SHOW')),

                              CONSTRAINT uk_reservations_number UNIQUE (reservation_number)
);

CREATE TABLE reservation_rooms (
                                   id BIGSERIAL PRIMARY KEY,
                                   reservation_id BIGINT NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
                                   room_type_id BIGINT NOT NULL REFERENCES room_types(id),
                                   room_id BIGINT REFERENCES rooms(id),
                                   rate_plan_id BIGINT REFERENCES rate_plans(id),
                                   guests_count INTEGER NOT NULL DEFAULT 1,
                                   price_per_night NUMERIC(12, 2) NOT NULL,
                                   status VARCHAR(30) NOT NULL DEFAULT 'RESERVED',
                                   created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT chk_reservation_rooms_guests_count CHECK (guests_count > 0),
                                   CONSTRAINT chk_reservation_rooms_price CHECK (price_per_night >= 0),

                                   CONSTRAINT chk_reservation_rooms_status
                                       CHECK (status IN ('RESERVED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED'))
);

CREATE TABLE payments (
                          id BIGSERIAL PRIMARY KEY,
                          reservation_id BIGINT NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
                          amount NUMERIC(12, 2) NOT NULL,
                          method VARCHAR(30) NOT NULL,
                          status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
                          transaction_reference VARCHAR(120),
                          paid_at TIMESTAMPTZ,
                          comment TEXT,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT chk_payments_amount CHECK (amount > 0),

                          CONSTRAINT chk_payments_method
                              CHECK (method IN ('CASH', 'CARD', 'BANK_TRANSFER', 'ONLINE')),

                          CONSTRAINT chk_payments_status
                              CHECK (status IN ('CREATED', 'PAID', 'FAILED', 'REFUNDED', 'CANCELLED'))
);

CREATE TABLE additional_services (
                                     id BIGSERIAL PRIMARY KEY,
                                     hotel_id BIGINT NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
                                     name VARCHAR(150) NOT NULL,
                                     description TEXT,
                                     price NUMERIC(12, 2) NOT NULL,
                                     status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                                     created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT chk_additional_services_price CHECK (price >= 0),
                                     CONSTRAINT chk_additional_services_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE reservation_services (
                                      id BIGSERIAL PRIMARY KEY,
                                      reservation_id BIGINT NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
                                      service_id BIGINT NOT NULL REFERENCES additional_services(id),
                                      quantity INTEGER NOT NULL DEFAULT 1,
                                      price NUMERIC(12, 2) NOT NULL,
                                      total_price NUMERIC(12, 2) NOT NULL,
                                      created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      CONSTRAINT chk_reservation_services_quantity CHECK (quantity > 0),
                                      CONSTRAINT chk_reservation_services_price CHECK (price >= 0),
                                      CONSTRAINT chk_reservation_services_total_price CHECK (total_price >= 0)
);

CREATE TABLE employees (
                           id BIGSERIAL PRIMARY KEY,
                           hotel_id BIGINT NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
                           full_name VARCHAR(255) NOT NULL,
                           position VARCHAR(120) NOT NULL,
                           phone VARCHAR(50),
                           email VARCHAR(255),
                           status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT chk_employees_status
                               CHECK (status IN ('ACTIVE', 'INACTIVE', 'DISMISSED'))
);

CREATE TABLE user_accounts (
                               id BIGSERIAL PRIMARY KEY,
                               employee_id BIGINT NOT NULL UNIQUE REFERENCES employees(id) ON DELETE CASCADE,
                               username VARCHAR(100) NOT NULL UNIQUE,
                               password_hash VARCHAR(255) NOT NULL,
                               enabled BOOLEAN NOT NULL DEFAULT TRUE,
                               created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
                       id BIGSERIAL PRIMARY KEY,
                       code VARCHAR(50) NOT NULL UNIQUE,
                       name VARCHAR(120) NOT NULL
);

CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
                            role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,

                            CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id)
);

CREATE UNIQUE INDEX ux_guests_document
    ON guests (document_type, COALESCE(document_series, ''), document_number)
    WHERE document_type IS NOT NULL
      AND document_number IS NOT NULL;

CREATE INDEX idx_room_types_hotel_id ON room_types(hotel_id);
CREATE INDEX idx_rooms_hotel_id ON rooms(hotel_id);
CREATE INDEX idx_rooms_room_type_id ON rooms(room_type_id);
CREATE INDEX idx_guests_phone ON guests(phone);
CREATE INDEX idx_guests_email ON guests(email);
CREATE INDEX idx_rate_plans_hotel_id ON rate_plans(hotel_id);
CREATE INDEX idx_room_type_rates_room_type_id ON room_type_rates(room_type_id);
CREATE INDEX idx_room_type_rates_rate_plan_id ON room_type_rates(rate_plan_id);
CREATE INDEX idx_reservations_hotel_id ON reservations(hotel_id);
CREATE INDEX idx_reservations_guest_id ON reservations(guest_id);
CREATE INDEX idx_reservations_status ON reservations(status);
CREATE INDEX idx_reservations_dates ON reservations(check_in_date, check_out_date);
CREATE INDEX idx_reservation_rooms_reservation_id ON reservation_rooms(reservation_id);
CREATE INDEX idx_reservation_rooms_room_id ON reservation_rooms(room_id);
CREATE INDEX idx_payments_reservation_id ON payments(reservation_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_additional_services_hotel_id ON additional_services(hotel_id);
CREATE INDEX idx_reservation_services_reservation_id ON reservation_services(reservation_id);
CREATE INDEX idx_employees_hotel_id ON employees(hotel_id);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_hotels_set_updated_at
    BEFORE UPDATE ON hotels
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_room_types_set_updated_at
    BEFORE UPDATE ON room_types
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_rooms_set_updated_at
    BEFORE UPDATE ON rooms
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_guests_set_updated_at
    BEFORE UPDATE ON guests
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_rate_plans_set_updated_at
    BEFORE UPDATE ON rate_plans
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_reservations_set_updated_at
    BEFORE UPDATE ON reservations
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_reservation_rooms_set_updated_at
    BEFORE UPDATE ON reservation_rooms
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_payments_set_updated_at
    BEFORE UPDATE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_additional_services_set_updated_at
    BEFORE UPDATE ON additional_services
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_employees_set_updated_at
    BEFORE UPDATE ON employees
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_user_accounts_set_updated_at
    BEFORE UPDATE ON user_accounts
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

INSERT INTO roles (code, name) VALUES
                                   ('ADMIN', 'Администратор системы'),
                                   ('MANAGER', 'Менеджер гостиницы'),
                                   ('RECEPTIONIST', 'Администратор стойки регистрации'),
                                   ('HOUSEKEEPING', 'Сотрудник службы уборки'),
                                   ('ACCOUNTANT', 'Бухгалтер');