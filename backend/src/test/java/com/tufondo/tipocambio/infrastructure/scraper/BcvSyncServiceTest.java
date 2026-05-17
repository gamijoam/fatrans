package com.tufondo.tipocambio.infrastructure.scraper;

import com.tufondo.tipocambio.domain.model.TipoCambio;
import com.tufondo.tipocambio.domain.repository.TipoCambioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests para issue #231: orquestación del sync BCV.
 *
 * <p>Mockeamos {@link BcvScraperService} (no queremos red real en tests
 * unitarios) y verificamos que la lógica de persistencia es correcta.</p>
 */
@ExtendWith(MockitoExtension.class)
class BcvSyncServiceTest {

    @Mock
    private BcvScraperService bcvScraperService;

    @Mock
    private TipoCambioRepository tipoCambioRepository;

    @InjectMocks
    private BcvSyncService bcvSyncService;

    private LocalDate fechaBcv;
    private BigDecimal tasaBcv;
    private BcvScraperService.TasaScrapeada scrapeada;

    @BeforeEach
    void setUp() {
        fechaBcv = LocalDate.of(2026, 5, 19);
        tasaBcv = new BigDecimal("517.96190000");
        scrapeada = new BcvScraperService.TasaScrapeada(tasaBcv, fechaBcv);
    }

    @Test
    @DisplayName("Issue #231: tasa NO existe en BD → la inserta como fuente='BCV', compra==venta")
    void sincronizar_insertaCuandoNoExiste() {
        when(bcvScraperService.scrapearTasaBcv()).thenReturn(scrapeada);
        when(tipoCambioRepository.buscarPorFecha(fechaBcv)).thenReturn(Optional.empty());

        BcvSyncService.SincronizacionResultado r = bcvSyncService.sincronizarDesdeBcv();

        ArgumentCaptor<TipoCambio> captor = ArgumentCaptor.forClass(TipoCambio.class);
        verify(tipoCambioRepository).guardar(captor.capture());

        TipoCambio guardado = captor.getValue();
        assertThat(guardado.getFecha()).isEqualTo(fechaBcv);
        assertThat(guardado.getFuente()).isEqualTo("BCV");
        assertThat(guardado.getTasaCompra()).isEqualByComparingTo(tasaBcv);
        assertThat(guardado.getTasaVenta()).isEqualByComparingTo(tasaBcv);
        // BCV no publica spread → compra == venta
        assertThat(guardado.getTasaCompra()).isEqualByComparingTo(guardado.getTasaVenta());
        assertThat(guardado.getCreadoPor()).isNull();

        assertThat(r.insertada()).isTrue();
        assertThat(r.tasa()).isEqualByComparingTo(tasaBcv);
    }

    @Test
    @DisplayName("Issue #231: tasa YA existe para la fecha → NO duplica (idempotente)")
    void sincronizar_idempotenteCuandoYaExiste() {
        when(bcvScraperService.scrapearTasaBcv()).thenReturn(scrapeada);
        TipoCambio existente = TipoCambio.builder()
                .fecha(fechaBcv)
                .tasaCompra(tasaBcv)
                .tasaVenta(tasaBcv)
                .fuente("BCV")
                .build();
        when(tipoCambioRepository.buscarPorFecha(fechaBcv)).thenReturn(Optional.of(existente));

        BcvSyncService.SincronizacionResultado r = bcvSyncService.sincronizarDesdeBcv();

        // CRÍTICO: no debe llamar `guardar` cuando ya existe
        verify(tipoCambioRepository, never()).guardar(any());
        assertThat(r.insertada()).isFalse();
        assertThat(r.detalle()).contains("Ya existía");
    }

    @Test
    @DisplayName("Issue #231: si el scraper falla, la excepción se propaga (caller maneja)")
    void sincronizar_propagaErrorDelScraper() {
        when(bcvScraperService.scrapearTasaBcv()).thenThrow(
                new BcvScraperService.BcvScrapingException("BCV inalcanzable"));

        assertThatThrownBy(() -> bcvSyncService.sincronizarDesdeBcv())
                .isInstanceOf(BcvScraperService.BcvScrapingException.class)
                .hasMessageContaining("BCV inalcanzable");

        // Nada se guarda si el scraping falla
        verify(tipoCambioRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Issue #231: job programado captura excepciones (no rompe el scheduler)")
    void jobDiario_capturaExcepciones() {
        when(bcvScraperService.scrapearTasaBcv()).thenThrow(
                new BcvScraperService.BcvScrapingException("error simulado"));

        // No debe lanzar nada — el scheduler de Spring se rompería.
        bcvSyncService.ejecutarJobDiario();

        verify(tipoCambioRepository, never()).guardar(any());
    }
}
