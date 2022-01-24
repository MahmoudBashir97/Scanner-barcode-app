package com.mahmoudbashir.thirdwayvtaskchallange.dataLocal

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.mahmoudbashir.thirdwayvtaskchallange.model.ItemModel
import com.mahmoudbashir.thirdwayvtaskchallange.room.ItemDao
import com.mahmoudbashir.thirdwayvtaskchallange.room.ItemsDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest //to tell that is an Unit Test
class ItemDaoTest {

    @get:Rule
    var instantTaskExecutorRule=InstantTaskExecutorRule()

    private lateinit var database:ItemsDatabase
    private lateinit var dao:ItemDao


    @Before
    fun setUp(){
        // this is not a real database
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ItemsDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.dao()
    }


    @After
    fun teardown(){
        database.close()
    }

    @Test
    fun insertShoppingItem() = runBlockingTest {
        val item = ItemModel(1,"97012345","cola","Drink","28-1-2022",1024)

        dao.insertNewItem(item)
        val allShoppingItems = dao.getAllItems().getOrAwaitValue()

        assertThat(allShoppingItems).contains(item)
    }

    @Test
    fun deleteShoppingItem() = runBlockingTest {
        val item = ItemModel(1,"97012345","cola","Drink","28-1-2022",1024)

        dao.insertNewItem(item)
        dao.deleteSingleItem(item)

        val allShoppingItems = dao.getAllItems().getOrAwaitValue()

        assertThat(allShoppingItems).doesNotContain(item)
    }
}