package com.proedobar.siscep;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class RegisterPagerAdapter extends FragmentStateAdapter {

    public RegisterPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Retornar el fragmento correspondiente según la posición
        switch (position) {
            case 0:
                return new RegisterStep1Fragment();
            case 1:
                return new RegisterStep2Fragment();
            case 2:
                return new RegisterStep3Fragment();
            default:
                return new RegisterStep1Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Número total de páginas
    }
} 