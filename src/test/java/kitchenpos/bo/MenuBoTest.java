package kitchenpos.bo;

import kitchenpos.dao.MenuDao;
import kitchenpos.dao.MenuGroupDao;
import kitchenpos.dao.MenuProductDao;
import kitchenpos.dao.ProductDao;
import kitchenpos.model.Menu;
import kitchenpos.model.MenuProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class MenuBoTest {

    @Mock
    private MenuDao menuDao;
    @Mock
    private MenuGroupDao menuGroupDao;
    @Mock
    private MenuProductDao menuProductDao;
    @Mock
    private ProductDao productDao;

    @InjectMocks
    private MenuBo menuBo;

    private Menu menuExpected;

    @BeforeEach
    void setUp() {
        menuExpected = new Menu();
        menuExpected.setId(1L);
        menuExpected.setName("고기");
        menuExpected.setPrice(BigDecimal.valueOf(1000));
        menuExpected.setMenuGroupId(2L);
        menuExpected.setMenuProducts(Collections.emptyList());
    }

    @Test
    @DisplayName("메뉴 가격은 0원 이하일 때")
    void createMenuByValidationPrice() {
        // give
        Menu menuActual = new Menu();
        menuActual.setPrice(BigDecimal.valueOf(0));
        // when then
        assertThatIllegalArgumentException().isThrownBy(() -> menuBo.create(menuActual));
    }

    @Test
    @DisplayName("메뉴 그룹에 등록되지 않으면 등록할 수 없다.")
    void createMenuByValidationMenuGroup() {
        // give
        given(menuGroupDao.existsById(2L))
                .willReturn(false);
        // when then
        assertThatIllegalArgumentException().isThrownBy(() -> menuBo.create(menuExpected));
    }

    @Test
    @DisplayName("등록되지 않은 상품은 메뉴에 등록할 수 없다")
    void createMenuByValidationProduct() {
        // give
        MenuProduct menuProductExpected = new MenuProduct();
        menuProductExpected.setMenuId(1L);
        menuProductExpected.setProductId(1L);
        menuProductExpected.setQuantity(1L);

        menuExpected.setMenuProducts(Arrays.asList(menuProductExpected));
        menuExpected.setPrice(BigDecimal.valueOf(0));

        given(menuGroupDao.existsById(2L))
                .willReturn(true);

        given(productDao.findById(1L))
                .willReturn(null);
        // when then
        assertThatNullPointerException().isThrownBy(() -> menuBo.create(menuExpected));
    }

    @Test
    @DisplayName("메뉴 가격은 등록된 상품들의 가격 합보다 작아야한다.")
    void createMenuByValidationMenuPriceOver() {
        // give
        given(menuGroupDao.existsById(2L))
                .willReturn(true);
        // when then
        assertThatIllegalArgumentException().isThrownBy(() -> menuBo.create(menuExpected));
    }

    @Test
    @DisplayName("메뉴 등록")
    void create() {
        // give
        given(menuGroupDao.existsById(2L))
                .willReturn(true);
        given(menuDao.save(menuExpected))
                .willReturn(menuExpected);
        menuExpected.setPrice(BigDecimal.valueOf(0));
        Menu menuExpected = this.menuExpected;

        // when
        Menu menuActual = menuBo.create(this.menuExpected);
        // then
        assertThat(menuActual.getId()).isEqualTo(menuExpected.getId());
        assertThat(menuActual.getName()).isEqualTo(menuExpected.getName());
    }

    @Test
    @DisplayName("등록된 메뉴들을 조회할 수 있다.")
    void getMenus() {
        // give
        MenuProduct menuProductExpected = new MenuProduct();
        menuProductExpected.setMenuId(1L);

        given(menuDao.findAll())
                .willReturn(Arrays.asList(menuExpected));

        given(menuProductDao.findAllByMenuId(menuExpected.getId()))
                .willReturn(Arrays.asList(menuProductExpected));

        List<Menu> menusExpected = Arrays.asList(menuExpected);
        // when
        List<Menu> menusActual = menuBo.list();
        // then
        assertAll("menuExpected test", () ->
                assertAll("first menuExpected name test", () -> {
                    int firstIndex = 0;
                    assertThat(menusActual.get(firstIndex).getName()).isEqualTo(menusExpected.get(firstIndex).getName());
                    assertThat(menusActual.size()).isEqualTo(menusExpected.size());
                })
        );
    }
}
