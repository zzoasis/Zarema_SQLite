package com.example.zarema_sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class MySQLite extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "computer";
    static final String TABLE_NAME = "emergency_service";
    static final String ID = "id";
    static final String COMPUTER = "computer";
    static final String COMPUTER_LC = "computer_lc"; // // Поле с цитатой в нижнем регистре
    static final String PRICE = "price";
    static final String ASSETS_FILE_NAME = "computer.txt";
    static final String DATA_SEPARATOR = "@";
    private static final int DATABASE_VERSION = 2;
    private final Context context;

    public MySQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + ID + " INTEGER PRIMARY KEY,"
                + COMPUTER + " TEXT,"
                + COMPUTER_LC + " TEXT,"
                + PRICE + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
        loadDataFromAsset(context, ASSETS_FILE_NAME, db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Добавление новой цитаты в БД
    public void addData(SQLiteDatabase db, String computer, String price) {
        ContentValues values = new ContentValues();
        values.put(COMPUTER, computer);
        values.put(COMPUTER_LC, computer.toLowerCase());
        values.put(PRICE, price);
        db.insert(TABLE_NAME, null, values);
    }

    // Добавление записей в базу данных из файла ресурсов
    public void loadDataFromAsset(Context context, String fileName, SQLiteDatabase db) {
        BufferedReader in = null;

        try {
            // Открываем поток для работы с файлом с исходными данными
            InputStream is = context.getAssets().open(fileName);
            // Открываем буфер обмена для потока работы с файлом с исходными данными
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            while ((str = in.readLine()) != null) { // Читаем строку из файла
                String strTrim = str.trim(); // Убираем у строки пробелы с концов
                if (!strTrim.equals("")) { // Если строка не пустая, то
                    StringTokenizer st = new StringTokenizer(strTrim, DATA_SEPARATOR); // Нарезаем ее на части
                    String computer = st.nextToken().trim(); // Извлекаем из строки название организации без пробелов на концах
                    String price = st.nextToken().trim(); // Извлекаем из строки номер организации без пробелов на концах
                    addData(db, computer, price); // Добавляем название и цену в базу данных
                }
            }

            // Обработчики ошибок
        } catch (IOException ignored) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }

    }

    // Получение значений данных из БД в виде строки с фильтром
    public String getData(String filter) {

        String selectQuery; // Переменная для SQL-запроса

        if (filter.equals("")) {
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " + COMPUTER;
        } else {
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE (" + COMPUTER_LC + " LIKE '%" +
                    filter.toLowerCase() + "%'" +
                    " OR " + PRICE + " LIKE '%" + filter + "%'" + ") ORDER BY " + COMPUTER;
        }
        SQLiteDatabase db = this.getReadableDatabase(); // Доступ к БД
        Cursor cursor = db.rawQuery(selectQuery, null); // Выполнение SQL-запроса

        StringBuilder data = new StringBuilder(); // Переменная для формирования данных из запроса

        int num = 0;
        if (cursor.moveToFirst()) { // Если есть хоть одна запись, то
            do { // Цикл по всем записям результата запроса
                String computer = cursor.getString(1); // Чтение цитатой
                String price = cursor.getString(3); // Чтение автора цитаты
                data.append(++num).append(") ").append(computer).append(". Цена: ").append(price).append(" тг").append("\n");
            } while (cursor.moveToNext()); // Цикл пока есть следующая запись
        }
        return data.toString(); // Возвращение результата
    }

}