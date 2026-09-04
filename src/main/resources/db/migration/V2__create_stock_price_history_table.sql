CREATE TABLE IF NOT EXISTS stock_price_history (
    id              BIGSERIAL PRIMARY KEY,
    stock_id        BIGINT          NOT NULL REFERENCES stocks(id) ON DELETE CASCADE,
    symbol          VARCHAR(16)     NOT NULL,
    price           NUMERIC(18,4)   NOT NULL,
    volume          BIGINT          NOT NULL DEFAULT 0,
    change_percent  NUMERIC(8,4)    NOT NULL DEFAULT 0,
    ts              TIMESTAMPTZ     NOT NULL DEFAULT now()
);

-- Advanced native queries below rely heavily on these indexes
CREATE INDEX IF NOT EXISTS idx_sph_symbol_ts ON stock_price_history (symbol, ts DESC);
CREATE INDEX IF NOT EXISTS idx_sph_stock_id ON stock_price_history (stock_id);
CREATE INDEX IF NOT EXISTS idx_sph_ts ON stock_price_history (ts DESC);
