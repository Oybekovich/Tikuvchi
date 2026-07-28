-- Usta interfeysi uchun: sozlamalar va buyurtma tarixi

ALTER TABLE usta_profiles ADD COLUMN IF NOT EXISTS available boolean DEFAULT true;
ALTER TABLE usta_profiles ADD COLUMN IF NOT EXISTS visible boolean DEFAULT true;

CREATE TABLE IF NOT EXISTS order_events (
  id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  order_id    uuid REFERENCES orders(id) ON DELETE CASCADE,
  from_status order_status,
  to_status   order_status,
  changed_by  uuid REFERENCES profiles(id),
  comment     text,
  created_at  timestamptz DEFAULT now()
);

ALTER TABLE order_events ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Order events are viewable by participants"
  ON order_events FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM orders
      WHERE orders.id = order_events.order_id
        AND (orders.client_id = auth.uid() OR orders.usta_id = auth.uid())
    )
  );

CREATE POLICY "Ustas can insert order events"
  ON order_events FOR INSERT
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM orders
      WHERE orders.id = order_events.order_id
        AND orders.usta_id = auth.uid()
    )
  );
