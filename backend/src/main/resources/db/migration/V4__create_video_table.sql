CREATE TABLE IF NOT EXISTS videos
(
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    description      TEXT,
    file_path        VARCHAR(500) NOT NULL UNIQUE,
    file_size        BIGINT       NOT NULL,
    duration_seconds INTEGER,
    format           VARCHAR(20)  NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    mime_type        VARCHAR(50)  NOT NULL,
    encryption_key   VARCHAR(500) NOT NULL,
    total_chunks     INTEGER      NOT NULL,
    chunk_size       INTEGER      NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_file_size_positive CHECK (file_size > 0),
    CONSTRAINT check_duration_positive CHECK (duration_seconds IS NULL OR duration_seconds >= 0),
    CONSTRAINT check_total_chunks_positive CHECK (total_chunks > 0),
    CONSTRAINT check_chunk_size_positive CHECK (chunk_size > 0),
    CONSTRAINT check_format_valid CHECK (format IN ('MP4', 'WEBM', 'MOV', 'AVI')),
    CONSTRAINT check_status_valid CHECK (status IN ('PENDING', 'PROCESSING', 'READY', 'ERROR', 'DELETED'))
);

CREATE INDEX IF NOT EXISTS idx_videos_status ON videos (status);
CREATE INDEX IF NOT EXISTS idx_videos_format ON videos (format);
CREATE INDEX IF NOT EXISTS idx_videos_created_at ON videos (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_videos_file_path ON videos (file_path);

COMMENT ON TABLE videos IS 'Таблица для хранения метаданных видео файлов';
COMMENT ON COLUMN videos.id IS 'Уникальный идентификатор видео';
COMMENT ON COLUMN videos.title IS 'Название видео';
COMMENT ON COLUMN videos.description IS 'Описание видео';
COMMENT ON COLUMN videos.file_path IS 'Путь к видео файлу в файловой системе';
COMMENT ON COLUMN videos.file_size IS 'Размер файла в байтах';
COMMENT ON COLUMN videos.duration_seconds IS 'Длительность видео в секундах';
COMMENT ON COLUMN videos.format IS 'Формат видео файла';
COMMENT ON COLUMN videos.status IS 'Текущий статус обработки видео';
COMMENT ON COLUMN videos.mime_type IS 'MIME тип видео файла';
COMMENT ON COLUMN videos.encryption_key IS 'Ключ шифрования для данного видео (Base64)';
COMMENT ON COLUMN videos.total_chunks IS 'Общее количество чанков в видео';
COMMENT ON COLUMN videos.chunk_size IS 'Размер одного чанка в байтах';
COMMENT ON COLUMN videos.created_at IS 'Дата и время создания записи';
COMMENT ON COLUMN videos.updated_at IS 'Дата и время последнего обновления';