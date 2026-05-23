package com.foodstore.service;

import com.foodstore.dto.response.AdminStatsResponse;
import com.foodstore.repository.CategoriaRepository;
import com.foodstore.repository.PedidoRepository;
import com.foodstore.repository.ProductoRepository;
import com.foodstore.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;

    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
            categoriaRepository.countActive(),
            productoRepository.countActive(),
            pedidoRepository.countActive(),
            usuarioRepository.countActive()
        );
    }
}
