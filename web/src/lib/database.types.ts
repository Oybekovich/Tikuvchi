export type Json =
  | string
  | number
  | boolean
  | null
  | { [key: string]: Json | undefined }
  | Json[];

export type Database = {
  __InternalSupabase: {
    PostgrestVersion: "14.5";
  };
  public: {
    Tables: {
      conversations: {
        Row: {
          client_id: string;
          id: string;
          last_message_at: string;
          usta_id: string;
        };
        Insert: {
          client_id: string;
          id?: string;
          last_message_at?: string;
          usta_id: string;
        };
        Update: {
          client_id?: string;
          id?: string;
          last_message_at?: string;
          usta_id?: string;
        };
        Relationships: [
          {
            foreignKeyName: "conversations_client_id_fkey";
            columns: ["client_id"];
            isOneToOne: false;
            referencedRelation: "profiles";
            referencedColumns: ["id"];
          },
          {
            foreignKeyName: "conversations_usta_id_fkey";
            columns: ["usta_id"];
            isOneToOne: false;
            referencedRelation: "usta_profiles";
            referencedColumns: ["user_id"];
          },
        ];
      };
      measurements: {
        Row: {
          chest: number | null;
          client_id: string;
          height: number | null;
          hips: number | null;
          id: string;
          label: string;
          notes: string | null;
          shoulder: number | null;
          sleeve_length: number | null;
          updated_at: string;
          waist: number | null;
        };
        Insert: {
          chest?: number | null;
          client_id: string;
          height?: number | null;
          hips?: number | null;
          id?: string;
          label: string;
          notes?: string | null;
          shoulder?: number | null;
          sleeve_length?: number | null;
          updated_at?: string;
          waist?: number | null;
        };
        Update: {
          chest?: number | null;
          client_id?: string;
          height?: number | null;
          hips?: number | null;
          id?: string;
          label?: string;
          notes?: string | null;
          shoulder?: number | null;
          sleeve_length?: number | null;
          updated_at?: string;
          waist?: number | null;
        };
        Relationships: [
          {
            foreignKeyName: "measurements_client_id_fkey";
            columns: ["client_id"];
            isOneToOne: false;
            referencedRelation: "profiles";
            referencedColumns: ["id"];
          },
        ];
      };
      messages: {
        Row: {
          content: string | null;
          conversation_id: string;
          created_at: string;
          id: string;
          message_type: Database["public"]["Enums"]["message_type"];
          price_offer_amount: number | null;
          price_offer_duration_days: number | null;
          price_offer_note: string | null;
          price_offer_status:
            | Database["public"]["Enums"]["price_offer_status"]
            | null;
          sender_id: string;
        };
        Insert: {
          content?: string | null;
          conversation_id: string;
          created_at?: string;
          id?: string;
          message_type?: Database["public"]["Enums"]["message_type"];
          price_offer_amount?: number | null;
          price_offer_duration_days?: number | null;
          price_offer_note?: string | null;
          price_offer_status?:
            | Database["public"]["Enums"]["price_offer_status"]
            | null;
          sender_id: string;
        };
        Update: {
          content?: string | null;
          conversation_id?: string;
          created_at?: string;
          id?: string;
          message_type?: Database["public"]["Enums"]["message_type"];
          price_offer_amount?: number | null;
          price_offer_duration_days?: number | null;
          price_offer_note?: string | null;
          price_offer_status?:
            | Database["public"]["Enums"]["price_offer_status"]
            | null;
          sender_id?: string;
        };
        Relationships: [
          {
            foreignKeyName: "messages_conversation_id_fkey";
            columns: ["conversation_id"];
            isOneToOne: false;
            referencedRelation: "conversations";
            referencedColumns: ["id"];
          },
          {
            foreignKeyName: "messages_sender_id_fkey";
            columns: ["sender_id"];
            isOneToOne: false;
            referencedRelation: "profiles";
            referencedColumns: ["id"];
          },
        ];
      };
      order_items: {
        Row: {
          id: number;
          image_url: string | null;
          material: string | null;
          model_note: string | null;
          order_id: string;
          price: number;
          size_note: string | null;
          title: string;
        };
        Insert: {
          id?: never;
          image_url?: string | null;
          material?: string | null;
          model_note?: string | null;
          order_id: string;
          price?: number;
          size_note?: string | null;
          title: string;
        };
        Update: {
          id?: never;
          image_url?: string | null;
          material?: string | null;
          model_note?: string | null;
          order_id?: string;
          price?: number;
          size_note?: string | null;
          title?: string;
        };
        Relationships: [
          {
            foreignKeyName: "order_items_order_id_fkey";
            columns: ["order_id"];
            isOneToOne: false;
            referencedRelation: "orders";
            referencedColumns: ["id"];
          },
        ];
      };
      orders: {
        Row: {
          client_id: string;
          created_at: string;
          estimated_ready_at: string | null;
          id: string;
          payment_status: Database["public"]["Enums"]["payment_status"];
          source: Database["public"]["Enums"]["order_source"];
          status: Database["public"]["Enums"]["order_status"];
          total_price: number;
          usta_id: string;
        };
        Insert: {
          client_id: string;
          created_at?: string;
          estimated_ready_at?: string | null;
          id?: string;
          payment_status?: Database["public"]["Enums"]["payment_status"];
          source?: Database["public"]["Enums"]["order_source"];
          status?: Database["public"]["Enums"]["order_status"];
          total_price?: number;
          usta_id: string;
        };
        Update: {
          client_id?: string;
          created_at?: string;
          estimated_ready_at?: string | null;
          id?: string;
          payment_status?: Database["public"]["Enums"]["payment_status"];
          source?: Database["public"]["Enums"]["order_source"];
          status?: Database["public"]["Enums"]["order_status"];
          total_price?: number;
          usta_id?: string;
        };
        Relationships: [
          {
            foreignKeyName: "orders_client_id_fkey";
            columns: ["client_id"];
            isOneToOne: false;
            referencedRelation: "profiles";
            referencedColumns: ["id"];
          },
          {
            foreignKeyName: "orders_usta_id_fkey";
            columns: ["usta_id"];
            isOneToOne: false;
            referencedRelation: "usta_profiles";
            referencedColumns: ["user_id"];
          },
        ];
      };
      portfolio_items: {
        Row: {
          caption: string | null;
          id: number;
          image_url: string;
          sort_order: number;
          usta_id: string;
        };
        Insert: {
          caption?: string | null;
          id?: never;
          image_url: string;
          sort_order?: number;
          usta_id: string;
        };
        Update: {
          caption?: string | null;
          id?: never;
          image_url?: string;
          sort_order?: number;
          usta_id?: string;
        };
        Relationships: [
          {
            foreignKeyName: "portfolio_items_usta_id_fkey";
            columns: ["usta_id"];
            isOneToOne: false;
            referencedRelation: "usta_profiles";
            referencedColumns: ["user_id"];
          },
        ];
      };
      profiles: {
        Row: {
          avatar_url: string | null;
          created_at: string;
          full_name: string;
          id: string;
          phone: string | null;
          role: Database["public"]["Enums"]["user_role"];
        };
        Insert: {
          avatar_url?: string | null;
          created_at?: string;
          full_name: string;
          id: string;
          phone?: string | null;
          role?: Database["public"]["Enums"]["user_role"];
        };
        Update: {
          avatar_url?: string | null;
          created_at?: string;
          full_name?: string;
          id?: string;
          phone?: string | null;
          role?: Database["public"]["Enums"]["user_role"];
        };
        Relationships: [];
      };
      reviews: {
        Row: {
          client_id: string;
          comment: string | null;
          created_at: string;
          id: number;
          rating: number;
          usta_id: string;
        };
        Insert: {
          client_id: string;
          comment?: string | null;
          created_at?: string;
          id?: never;
          rating: number;
          usta_id: string;
        };
        Update: {
          client_id?: string;
          comment?: string | null;
          created_at?: string;
          id?: never;
          rating?: number;
          usta_id?: string;
        };
        Relationships: [
          {
            foreignKeyName: "reviews_client_id_fkey";
            columns: ["client_id"];
            isOneToOne: false;
            referencedRelation: "profiles";
            referencedColumns: ["id"];
          },
          {
            foreignKeyName: "reviews_usta_id_fkey";
            columns: ["usta_id"];
            isOneToOne: false;
            referencedRelation: "usta_profiles";
            referencedColumns: ["user_id"];
          },
        ];
      };
      service_categories: {
        Row: {
          gender_segment: Database["public"]["Enums"]["gender_segment"];
          icon: string | null;
          id: number;
          name: string;
        };
        Insert: {
          gender_segment?: Database["public"]["Enums"]["gender_segment"];
          icon?: string | null;
          id?: never;
          name: string;
        };
        Update: {
          gender_segment?: Database["public"]["Enums"]["gender_segment"];
          icon?: string | null;
          id?: never;
          name?: string;
        };
        Relationships: [];
      };
      usta_profiles: {
        Row: {
          bio: string | null;
          cover_image_url: string | null;
          district: string | null;
          gender_segment: Database["public"]["Enums"]["gender_segment"];
          location_text: string | null;
          rating_avg: number;
          rating_count: number;
          tags: string[];
          user_id: string;
          work_hours_end: string | null;
          work_hours_start: string | null;
        };
        Insert: {
          bio?: string | null;
          cover_image_url?: string | null;
          district?: string | null;
          gender_segment?: Database["public"]["Enums"]["gender_segment"];
          location_text?: string | null;
          rating_avg?: number;
          rating_count?: number;
          tags?: string[];
          user_id: string;
          work_hours_end?: string | null;
          work_hours_start?: string | null;
        };
        Update: {
          bio?: string | null;
          cover_image_url?: string | null;
          district?: string | null;
          gender_segment?: Database["public"]["Enums"]["gender_segment"];
          location_text?: string | null;
          rating_avg?: number;
          rating_count?: number;
          tags?: string[];
          user_id?: string;
          work_hours_end?: string | null;
          work_hours_start?: string | null;
        };
        Relationships: [
          {
            foreignKeyName: "usta_profiles_user_id_fkey";
            columns: ["user_id"];
            isOneToOne: true;
            referencedRelation: "profiles";
            referencedColumns: ["id"];
          },
        ];
      };
      usta_services: {
        Row: {
          base_price: number;
          category_id: number | null;
          description: string | null;
          id: number;
          title: string;
          usta_id: string;
        };
        Insert: {
          base_price: number;
          category_id?: number | null;
          description?: string | null;
          id?: never;
          title: string;
          usta_id: string;
        };
        Update: {
          base_price?: number;
          category_id?: number | null;
          description?: string | null;
          id?: never;
          title?: string;
          usta_id?: string;
        };
        Relationships: [
          {
            foreignKeyName: "usta_services_category_id_fkey";
            columns: ["category_id"];
            isOneToOne: false;
            referencedRelation: "service_categories";
            referencedColumns: ["id"];
          },
          {
            foreignKeyName: "usta_services_usta_id_fkey";
            columns: ["usta_id"];
            isOneToOne: false;
            referencedRelation: "usta_profiles";
            referencedColumns: ["user_id"];
          },
        ];
      };
    };
    Views: {
      [_ in never]: never;
    };
    Functions: {
      [_ in never]: never;
    };
    Enums: {
      gender_segment: "women" | "men" | "unisex";
      message_type: "text" | "price_offer" | "image";
      order_source: "catalog" | "chat_negotiation";
      order_status:
        | "pending"
        | "accepted"
        | "in_progress"
        | "ready"
        | "completed"
        | "cancelled";
      payment_status: "pending" | "partial" | "paid";
      price_offer_status: "pending" | "accepted" | "declined";
      user_role: "client" | "usta";
    };
    CompositeTypes: {
      [_ in never]: never;
    };
  };
};

type DefaultSchema = Database["public"];

export type Tables<T extends keyof DefaultSchema["Tables"]> =
  DefaultSchema["Tables"][T]["Row"];
export type TablesInsert<T extends keyof DefaultSchema["Tables"]> =
  DefaultSchema["Tables"][T]["Insert"];
export type TablesUpdate<T extends keyof DefaultSchema["Tables"]> =
  DefaultSchema["Tables"][T]["Update"];
export type Enums<T extends keyof DefaultSchema["Enums"]> =
  DefaultSchema["Enums"][T];
