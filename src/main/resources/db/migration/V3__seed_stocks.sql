MERGE INTO stocks AS target
USING (
    VALUES

    -- ============================================================
    -- INDONESIA - IDX
    -- ============================================================

    -- Banking
    ('BBCA', 'Bank Central Asia Tbk',               'Financials',             'IDX', 'IDR', 10250, 10250, 10250, 10250, 10250, 0),
    ('BBRI', 'Bank Rakyat Indonesia (Persero) Tbk', 'Financials',             'IDX', 'IDR', 4500,  4500,  4500,  4500,  4500,  0),
    ('BMRI', 'Bank Mandiri (Persero) Tbk',          'Financials',             'IDX', 'IDR', 6500,  6500,  6500,  6500,  6500,  0),
    ('BBNI', 'Bank Negara Indonesia (Persero) Tbk', 'Financials',             'IDX', 'IDR', 5200,  5200,  5200,  5200,  5200,  0),
    ('BRIS', 'Bank Syariah Indonesia Tbk',          'Financials',             'IDX', 'IDR', 2800,  2800,  2800,  2800,  2800,  0),
    ('BBTN', 'Bank Tabungan Negara (Persero) Tbk',  'Financials',             'IDX', 'IDR', 1500,  1500,  1500,  1500,  1500,  0),

    -- Telecommunications
    ('TLKM', 'Telkom Indonesia (Persero) Tbk',      'Communication Services', 'IDX', 'IDR', 3000,  3000,  3000,  3000,  3000,  0),
    ('ISAT', 'Indosat Ooredoo Hutchison Tbk',       'Communication Services', 'IDX', 'IDR', 2400,  2400,  2400,  2400,  2400,  0),
    ('EXCL', 'XL Axiata Tbk',                       'Communication Services', 'IDX', 'IDR', 2300,  2300,  2300,  2300,  2300,  0),

    -- Consumer
    ('ICBP', 'Indofood CBP Sukses Makmur Tbk',      'Consumer Staples',       'IDX', 'IDR', 11200, 11200, 11200, 11200, 11200, 0),
    ('INDF', 'Indofood Sukses Makmur Tbk',          'Consumer Staples',       'IDX', 'IDR', 7200,  7200,  7200,  7200,  7200,  0),
    ('UNVR', 'Unilever Indonesia Tbk',              'Consumer Staples',       'IDX', 'IDR', 2300,  2300,  2300,  2300,  2300,  0),
    ('MYOR', 'Mayora Indah Tbk',                    'Consumer Staples',       'IDX', 'IDR', 2500,  2500,  2500,  2500,  2500,  0),
    ('AMRT', 'Sumber Alfaria Trijaya Tbk',          'Consumer Staples',       'IDX', 'IDR', 2200,  2200,  2200,  2200,  2200,  0),
    ('KLBF', 'Kalbe Farma Tbk',                     'Health Care',            'IDX', 'IDR', 1500,  1500,  1500,  1500,  1500, 0),

    -- Automotive / Industrial
    ('ASII', 'Astra International Tbk',             'Industrials',             'IDX', 'IDR', 5000,  5000,  5000,  5000,  5000,  0),
    ('AUTO', 'Astra Otoparts Tbk',                  'Consumer Discretionary', 'IDX', 'IDR', 2500,  2500,  2500,  2500,  2500,  0),

    -- Mining / Energy
    ('ANTM', 'Aneka Tambang Tbk',                   'Materials',              'IDX', 'IDR', 3000,  3000,  3000,  3000,  3000,  0),
    ('INCO', 'Vale Indonesia Tbk',                  'Materials',              'IDX', 'IDR', 4000,  4000,  4000,  4000,  4000,  0),
    ('MDKA', 'Merdeka Copper Gold Tbk',             'Materials',              'IDX', 'IDR', 2500,  2500,  2500,  2500,  2500,  0),
    ('PTBA', 'Bukit Asam Tbk',                      'Energy',                 'IDX', 'IDR', 3000,  3000,  3000,  3000,  3000,  0),
    ('ADRO', 'Alamtri Resources Indonesia Tbk',     'Energy',                 'IDX', 'IDR', 2500,  2500,  2500,  2500,  2500,  0),
    ('ITMG', 'Indo Tambangraya Megah Tbk',          'Energy',                 'IDX', 'IDR', 25000, 25000, 25000, 25000, 25000, 0),
    ('PGAS', 'Perusahaan Gas Negara Tbk',           'Energy',                 'IDX', 'IDR', 1600,  1600,  1600,  1600,  1600,  0),

    -- Technology
    ('GOTO', 'GoTo Gojek Tokopedia Tbk',            'Technology',             'IDX', 'IDR', 60,    60,    60,    60,    60,    0),
    ('BUKA', 'Bukalapak.com Tbk',                   'Technology',             'IDX', 'IDR', 120,   120,   120,   120,   120,   0),

    -- Petrochemical
    ('BRPT', 'Barito Pacific Tbk',                  'Materials',              'IDX', 'IDR', 1000,  1000,  1000,  1000,  1000,  0),
    ('TPIA', 'Chandra Asri Pacific Tbk',            'Materials',              'IDX', 'IDR', 8000,  8000,  8000,  8000,  8000,  0),

    -- Poultry
    ('CPIN', 'Charoen Pokphand Indonesia Tbk',      'Consumer Discretionary', 'IDX', 'IDR', 5000,  5000,  5000,  5000,  5000,  0),
    ('JPFA', 'Japfa Comfeed Indonesia Tbk',         'Consumer Discretionary', 'IDX', 'IDR', 2000,  2000,  2000,  2000,  2000,  0),

    -- ============================================================
    -- UNITED STATES
    -- ============================================================

    ('AAPL',  'Apple Inc.',                 'Technology',            'NASDAQ', 'USD', 195.20, 195.20, 195.20, 195.20, 195.20, 0),
    ('MSFT',  'Microsoft Corporation',      'Technology',            'NASDAQ', 'USD', 425.50, 425.50, 425.50, 425.50, 425.50, 0),
    ('GOOGL', 'Alphabet Inc.',              'Technology',            'NASDAQ', 'USD', 168.30, 168.30, 168.30, 168.30, 168.30, 0),
    ('AMZN',  'Amazon.com Inc.',            'Consumer Discretionary', 'NASDAQ', 'USD', 186.10, 186.10, 186.10, 186.10, 186.10, 0),
    ('NVDA',  'NVIDIA Corporation',         'Technology',            'NASDAQ', 'USD', 118.40, 118.40, 118.40, 118.40, 118.40, 0),
    ('TSLA',  'Tesla Inc.',                 'Consumer Discretionary', 'NASDAQ', 'USD', 248.90, 248.90, 248.90, 248.90, 248.90, 0),
    ('META',  'Meta Platforms Inc.',        'Technology',            'NASDAQ', 'USD', 512.70, 512.70, 512.70, 512.70, 512.70, 0),
    ('NFLX',  'Netflix Inc.',              'Communication Services', 'NASDAQ', 'USD', 675.30, 675.30, 675.30, 675.30, 675.30, 0),
    ('JPM',   'JPMorgan Chase & Co.',       'Financials',             'NYSE',   'USD', 215.60, 215.60, 215.60, 215.60, 215.60, 0),
    ('BAC',   'Bank of America Corp.',      'Financials',             'NYSE',   'USD', 39.80,  39.80,  39.80,  39.80,  39.80,  0),
    ('V',     'Visa Inc.',                  'Financials',             'NYSE',   'USD', 275.40, 275.40, 275.40, 275.40, 275.40, 0),
    ('WMT',   'Walmart Inc.',               'Consumer Staples',       'NYSE',   'USD', 78.90,  78.90,  78.90,  78.90, 78.90,  0),
    ('JNJ',   'Johnson & Johnson',          'Health Care',            'NYSE',   'USD', 152.30, 152.30, 152.30, 152.30, 152.30, 0),
    ('PFE',   'Pfizer Inc.',                'Health Care',            'NYSE',   'USD', 28.40,  28.40,  28.40,  28.40, 28.40,  0),
    ('XOM',   'Exxon Mobil Corporation',    'Energy',                 'NYSE',   'USD', 115.20, 115.20, 115.20, 115.20, 115.20, 0),
    ('CVX',   'Chevron Corporation',        'Energy',                 'NYSE',   'USD', 158.70, 158.70, 158.70, 158.70, 158.70, 0),
    ('DIS',   'The Walt Disney Company',    'Communication Services', 'NYSE',   'USD', 95.60,  95.60,  95.60,  95.60, 95.60,  0),
    ('KO',    'The Coca-Cola Company',      'Consumer Staples',       'NYSE',   'USD', 64.30,  64.30,  64.30,  64.30, 64.30,  0),
    ('INTC',  'Intel Corporation',          'Technology',            'NASDAQ', 'USD', 22.90,  22.90,  22.90,  22.90, 22.90,  0),
    ('AMD',   'Advanced Micro Devices',     'Technology',            'NASDAQ', 'USD', 145.80, 145.80, 145.80, 145.80, 145.80, 0)

) AS source (
    symbol,
    company_name,
    sector,
    exchange,
    currency,
    base_price,
    last_price,
    day_open,
    day_high,
    day_low,
    day_volume
)
ON target.symbol = source.symbol

WHEN MATCHED THEN
    UPDATE SET
        company_name = source.company_name,
        sector       = source.sector,
        exchange     = source.exchange,
        currency     = source.currency,
        base_price   = source.base_price,
        last_price   = source.last_price,
        day_open     = source.day_open,
        day_high     = source.day_high,
        day_low      = source.day_low,
        day_volume   = source.day_volume

WHEN NOT MATCHED THEN
    INSERT (
        symbol,
        company_name,
        sector,
        exchange,
        currency,
        base_price,
        last_price,
        day_open,
        day_high,
        day_low,
        day_volume
    )
    VALUES (
        source.symbol,
        source.company_name,
        source.sector,
        source.exchange,
        source.currency,
        source.base_price,
        source.last_price,
        source.day_open,
        source.day_high,
        source.day_low,
        source.day_volume
    );