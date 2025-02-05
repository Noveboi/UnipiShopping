package com.example.unipishopping.core;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.unipishopping.domain.Product;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A singleton instance containing all the products. The products list is updated in realtime.
 */
public class ProductProvider {
    private static final String TAG = "Products Provider";
    private static ProductProvider instance;
    public static ProductProvider getInstance() {
        if (instance == null) {
            instance = new ProductProvider();
        }

        return instance;
    }

    private final Map<Integer, Product> products;

    private ProductProvider() {
        DatabaseReference productsReference = FirebaseDatabase
                .getInstance()
                .getReference("products");

        ProductListener listener = new ProductListener();
        productsReference.addChildEventListener(listener);

        products = new HashMap<>();
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    private class ProductListener implements ChildEventListener {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            whenValid(snapshot, p -> {
                Product previous = products.put(p.getId(), p);
                if (previous != null) {
                    Log.w(TAG, "Replaced existing product in onChildAdded().");
                } else {
                    Log.i(TAG, "Inserted product '" + p.getId() + "'.");
                }
            });
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            whenValid(snapshot, p -> {
                Product previous = products.put(p.getId(), p);
                if (previous != null) {
                    Log.i(TAG, "Changed product '" + p.getId() + "'.");
                }
                else {
                    Log.w(TAG, "No previous product present in onChildChanged().");
                }
            });
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            whenValid(snapshot, p -> {
                Product removedProduct = products.remove(p.getId());
                if (removedProduct == null) {
                    Log.w(TAG, "Didn't find product '" + p.getId() + "' to remove");
                }
                else {
                    Log.i(TAG, "Removed product '" + p.getId() + "'.");
                }
            });
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            // Not Implemented
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.e(TAG, "An error occurred: " + error.getMessage() + "\n Details: " + error.getDetails());
        }

        private void whenValid(DataSnapshot snapshot, Consumer<Product> callback) {
            Product product = snapshot.getValue(Product.class);
            if (product == null) {
                Log.e(TAG,
                        "Couldn't deserialize product from DataSnapshot! Key = '" +
                        snapshot.getKey() + "'. Ref = '" + snapshot.getRef().getKey() + "'.");
                return;
            }

            callback.accept(product);
        }
    }
}
