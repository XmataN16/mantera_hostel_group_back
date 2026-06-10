-- demo_seed_hotel_stock.sql
-- Скрипт рассчитан на уже созданную схему из Flyway V1__init_schema.sql

BEGIN;

-- На случай повторного запуска в тестовой БД.
-- Если у вас уже есть важные данные, эти строки лучше убрать.
TRUNCATE TABLE rooms RESTART IDENTITY CASCADE;
TRUNCATE TABLE room_types RESTART IDENTITY CASCADE;
TRUNCATE TABLE hotels RESTART IDENTITY CASCADE;

WITH inserted_hotels AS (
    INSERT INTO hotels (
        name,
        short_name,
        address,
        phone,
        email,
        timezone,
        status
    ) VALUES
        (
            'Hotel Mantera Grand',
            'Mantera Grand',
            'Россия, Краснодарский край, пгт. Сириус, пр-кт Континентальный, д. 6',
            '+7 (861) 200-00-01',
            'grand@mantera-hotel.ru',
            'Europe/Moscow',
            'ACTIVE'
        ),
        (
            'Hotel Mantera Panorama',
            'Mantera Panorama',
            'Россия, Краснодарский край, г. Сочи, ул. Морская, д. 18',
            '+7 (861) 200-00-02',
            'panorama@mantera-hotel.ru',
            'Europe/Moscow',
            'ACTIVE'
        ),
        (
            'Hotel Mantera Airport',
            'Mantera Airport',
            'Россия, Краснодарский край, пгт. Сириус, аэропортовая зона, 1',
            '+7 (861) 200-00-03',
            'airport@mantera-hotel.ru',
            'Europe/Moscow',
            'INACTIVE'
        )
    RETURNING id, name, short_name
),
grand_hotel AS (
    SELECT id
    FROM inserted_hotels
    WHERE short_name = 'Mantera Grand'
),
panorama_hotel AS (
    SELECT id
    FROM inserted_hotels
    WHERE short_name = 'Mantera Panorama'
),
airport_hotel AS (
    SELECT id
    FROM inserted_hotels
    WHERE short_name = 'Mantera Airport'
),
inserted_room_types AS (
    INSERT INTO room_types (
        hotel_id,
        code,
        name,
        description,
        capacity,
        base_price,
        status
    )
    SELECT h.id, rt.code, rt.name, rt.description, rt.capacity, rt.base_price, rt.status
    FROM grand_hotel h
    CROSS JOIN (
        VALUES
            ('STD',  'Стандарт',     'Базовый номер с одной большой кроватью', 2, 4500.00, 'ACTIVE'),
            ('DBL',  'Дабл',         'Номер с двуспальной кроватью',            2, 5200.00, 'ACTIVE'),
            ('LUX',  'Люкс',         'Просторный номер повышенной комфортности', 3, 8900.00, 'ACTIVE')
    ) AS rt(code, name, description, capacity, base_price, status)

    UNION ALL

    SELECT h.id, rt.code, rt.name, rt.description, rt.capacity, rt.base_price, rt.status
    FROM panorama_hotel h
    CROSS JOIN (
        VALUES
            ('STD',  'Стандарт',     'Стандартный номер с видом на город',      2, 4100.00, 'ACTIVE'),
            ('FAM',  'Семейный',     'Номер для семьи из 3-4 человек',          4, 6500.00, 'ACTIVE'),
            ('SUIT', 'Сьют',         'Улучшенный номер с гостиной зоной',       3, 9800.00, 'ACTIVE')
    ) AS rt(code, name, description, capacity, base_price, status)

    UNION ALL

    SELECT h.id, rt.code, rt.name, rt.description, rt.capacity, rt.base_price, rt.status
    FROM airport_hotel h
    CROSS JOIN (
        VALUES
            ('STD', 'Стандарт', 'Номер для краткосрочного размещения', 2, 3900.00, 'INACTIVE')
    ) AS rt(code, name, description, capacity, base_price, status)
    RETURNING id, hotel_id, code
)

INSERT INTO rooms (
    hotel_id,
    room_type_id,
    room_number,
    floor,
    status,
    housekeeping_status,
    comment
)
SELECT
    h.id AS hotel_id,
    rt.id AS room_type_id,
    room_data.room_number,
    room_data.floor,
    room_data.status,
    room_data.housekeeping_status,
    room_data.comment
FROM inserted_room_types rt
JOIN hotels h ON h.id = rt.hotel_id
JOIN LATERAL (
    VALUES
        -- Mantera Grand: 12 комнат
        ('101', 1, 'AVAILABLE',    'CLEAN',      'Тихая сторона'),
        ('102', 1, 'OCCUPIED',     'INSPECTED',  'Заселён на 2 ночи'),
        ('103', 1, 'AVAILABLE',    'DIRTY',      'Требует уборки'),
        ('201', 2, 'AVAILABLE',    'CLEAN',      NULL),
        ('202', 2, 'MAINTENANCE',  'CLEAN',      'Плановый ремонт кондиционера'),
        ('203', 2, 'AVAILABLE',    'INSPECTED',  NULL),
        ('301', 3, 'AVAILABLE',    'CLEAN',      'Вид на море'),
        ('302', 3, 'OUT_OF_SERVICE','DIRTY',     'Неисправность сантехники'),
        ('303', 3, 'AVAILABLE',    'CLEAN',      NULL),

        -- Mantera Panorama: 9 комнат
        ('1101', 1, 'AVAILABLE',    'CLEAN',      'Вид на город'),
        ('1102', 1, 'OCCUPIED',     'INSPECTED',  NULL),
        ('1103', 1, 'AVAILABLE',    'CLEAN',      NULL),
        ('1201', 2, 'AVAILABLE',    'CLEAN',      'Подходит для семьи'),
        ('1202', 2, 'AVAILABLE',    'DIRTY',      'После выезда'),
        ('1203', 2, 'MAINTENANCE',  'CLEAN',      'Проверка электрики'),
        ('1301', 3, 'AVAILABLE',    'CLEAN',      'Сюит с дополнительной зоной'),
        ('1302', 3, 'AVAILABLE',    'INSPECTED',  NULL),
        ('1303', 3, 'OCCUPIED',     'CLEAN',      NULL),

        -- Mantera Airport: 4 комнаты
        ('210', 1, 'AVAILABLE',     'CLEAN',      'Краткосрочное размещение'),
        ('211', 1, 'AVAILABLE',     'CLEAN',      NULL),
        ('212', 1, 'OUT_OF_SERVICE','DIRTY',      'Закрыт на обслуживание'),
        ('213', 1, 'AVAILABLE',     'INSPECTED',  NULL)
) AS room_data(room_number, floor, status, housekeeping_status, comment)
ON true
WHERE
    (
        h.short_name = 'Mantera Grand' AND room_data.room_number IN ('101','102','103','201','202','203','301','302','303')
    )
    OR
    (
        h.short_name = 'Mantera Panorama' AND room_data.room_number IN ('1101','1102','1103','1201','1202','1203','1301','1302','1303')
    )
    OR
    (
        h.short_name = 'Mantera Airport' AND room_data.room_number IN ('210','211','212','213')
    );

COMMIT;