CREATE TABLE event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payload TEXT,
    status VARCHAR(20),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_status ON event(status);