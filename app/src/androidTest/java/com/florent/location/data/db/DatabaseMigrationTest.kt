package com.florent.location.data.db

import android.content.ContentValues
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    private val testDbName = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @After
    fun tearDown() {
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(testDbName)
    }

    @Test
    fun migrate1To2_preservesKeyData() {
        helper.createDatabase(testDbName, 1).apply {
            insert("tenants", 0, ContentValues().apply {
                put("id", 1L)
                put("remoteId", "tenant-remote-1")
                put("firstName", "Alice")
                put("lastName", "Martin")
                put("phone", "+32470000000")
                put("email", "alice@example.com")
                put("status", "ACTIVE")
                put("createdAt", 1000L)
                put("updatedAt", 1000L)
                put("isDeleted", 0)
                put("dirty", 0)
            })

            insert("housings", 0, ContentValues().apply {
                put("id", 10L)
                put("remoteId", "housing-remote-1")
                put("addr_street", "Rue de la Loi")
                put("addr_number", "16")
                put("addr_box", "A")
                put("addr_zipCode", "1000")
                put("addr_city", "Bruxelles")
                put("addr_country", "BE")
                put("createdAt", 1000L)
                put("updatedAt", 1000L)
                put("isArchived", 0)
                put("rentCents", 120000L)
                put("chargesCents", 15000L)
                put("depositCents", 240000L)
                put("pebRating", "UNKNOWN")
                put("isDeleted", 0)
                put("dirty", 0)
            })

            insert("leases", 0, ContentValues().apply {
                put("id", 100L)
                put("remoteId", "lease-remote-1")
                put("housingId", 10L)
                put("tenantId", 1L)
                put("startDateEpochDay", 20000L)
                put("endDateEpochDay", 21000L)
                put("rentCents", 120000L)
                put("chargesCents", 15000L)
                put("depositCents", 240000L)
                put("rentDueDayOfMonth", 5)
                put("rentOverridden", 0)
                put("chargesOverridden", 0)
                put("depositOverridden", 0)
                put("housingRentCentsSnapshot", 120000L)
                put("housingChargesCentsSnapshot", 15000L)
                put("housingDepositCentsSnapshot", 240000L)
                put("createdAt", 1000L)
                put("updatedAt", 1000L)
                put("isDeleted", 0)
                put("dirty", 0)
            })

            insert("auth_session", 0, ContentValues().apply {
                put("id", 1)
                put("accessToken", "token")
                put("refreshToken", "refresh")
                put("userId", "user-1")
                put("email", "alice@example.com")
                put("expiresAtEpochSeconds", 999999999L)
            })

            close()
        }

        val db = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java,
            testDbName
        )
            .addMigrations(DatabaseMigrations.MIGRATION_1_2)
            .build()

        db.openHelper.writableDatabase

        db.query("SELECT id, firstName, lastName FROM tenants WHERE id = 1", null).use {
            assertEquals(true, it.moveToFirst())
            assertEquals("Alice", it.getString(1))
            assertEquals("Martin", it.getString(2))
        }

        db.query("SELECT id, rentCents FROM housings WHERE id = 10", null).use {
            assertEquals(true, it.moveToFirst())
            assertEquals(120000L, it.getLong(1))
        }

        db.query("SELECT id, housingId, tenantId FROM leases WHERE id = 100", null).use {
            assertEquals(true, it.moveToFirst())
            assertEquals(10L, it.getLong(1))
            assertEquals(1L, it.getLong(2))
        }

        db.query("SELECT userId, accessToken FROM auth_session WHERE id = 1", null).use {
            assertEquals(true, it.moveToFirst())
            assertEquals("user-1", it.getString(0))
            assertEquals("token", it.getString(1))
        }

        db.close()
    }
}
