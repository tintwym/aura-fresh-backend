-- Delivery snapshot on orders (copied from user profile at checkout)
ALTER TABLE orders ADD COLUMN IF NOT EXISTS delivery_address1 VARCHAR(255);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS delivery_address2 VARCHAR(255);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS delivery_unit VARCHAR(64);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS delivery_floor VARCHAR(64);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS delivery_city VARCHAR(128);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS delivery_state VARCHAR(64);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS delivery_country VARCHAR(64);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS delivery_zip_code VARCHAR(32);
