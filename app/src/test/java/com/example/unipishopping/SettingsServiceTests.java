package com.example.unipishopping;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.os.LocaleListCompat;

import com.example.unipishopping.core.SettingsService;
import com.example.unipishopping.domain.UserSettings;
import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SettingsServiceTests {

    private Context mockContext;
    private SharedPreferences mockSharedPreferences;
    private SharedPreferences.Editor mockEditor;
    private MockedStatic<Log> logMock;

    private static final String KEY = SettingsService.JSON_KEY;

    @Before
    public void setUp() {
        mockContext = Mockito.mock(Context.class);
        mockSharedPreferences = Mockito.mock(SharedPreferences.class);
        mockEditor = Mockito.mock(SharedPreferences.Editor.class);

        logMock = mockStatic(Log.class);

        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPreferences);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
    }

    @After
    public void cleanUp() {
        if (logMock != null) {
            logMock.close();
        }
    }

    @Test
    public void get_null_whenSharedPreferencesKeyDoesNotExist() {
        when(mockSharedPreferences.getString(KEY, "")).thenReturn("");

        UserSettings settings = SettingsService.get(mockContext);

        assertNull(settings);
    }

    @Test
    public void get_valid_whenFullJson() {
        UserSettings fullSettings = new UserSettings();
        fullSettings.setFirstName("Test");
        fullSettings.setLastName("Test");
        fullSettings.setLocaleLanguageCode("en");

        String validJson = new Gson().toJson(fullSettings);
        when(mockSharedPreferences.getString(KEY, "")).thenReturn(validJson);

        UserSettings settings = SettingsService.get(mockContext);

        assertNotNull(settings);
        assertEquals(fullSettings.getFirstName(), settings.getFirstName());
        assertEquals(fullSettings.getLastName(), settings.getLastName());
        assertEquals(fullSettings.getLocaleLanguageCode(), settings.getLocaleLanguageCode());
    }

    @Test
    public void get_valid_whenPartialJson() {
        String malformedJson = "{firstName:\"John\"}";
        when(mockSharedPreferences.getString(KEY, "")).thenReturn(malformedJson);

        UserSettings settings = SettingsService.get(mockContext);

        assertNotNull(settings);
        assertEquals("John", settings.getFirstName());
    }

    @Test
    public void save_storesSettingsAsJson_inSharedPreferences() {
        UserSettings settings = new UserSettings();
        settings.setFirstName("Makis");

        SettingsService.save(settings, mockContext);

        Gson gson = new Gson();
        String expectedJson = gson.toJson(settings);
        verify(mockEditor).putString(KEY, expectedJson);
        verify(mockEditor).apply();
    }

    @Test
    public void getLocale_emptyLocaleList_whenSettingsNotInSharedPreferences() {
        when(mockSharedPreferences.getString(KEY, "")).thenReturn("");

        LocaleListCompat localeList = SettingsService.getLocale(mockContext);

        assertEquals(LocaleListCompat.getEmptyLocaleList(), localeList);
    }

    @Test
    public void getLocale_validLocaleList_whenSettingsExist() {
        UserSettings settings = new UserSettings();
        settings.setLocaleLanguageCode("en");

        String validJson = new Gson().toJson(settings);
        when(mockSharedPreferences.getString(KEY, "")).thenReturn(validJson);

        LocaleListCompat localeList = SettingsService.getLocale(mockContext);

        assertNotNull(localeList);
        assertNotEquals(LocaleListCompat.getEmptyLocaleList(), localeList);
    }
}
