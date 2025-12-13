package com.grocerystore.config;

import com.grocerystore.entity.*;
import com.grocerystore.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Инициализация начальных данных при запуске приложения
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public DataInitializer(UserRepository userRepository,
                          CategoryRepository categoryRepository,
                          SupplierRepository supplierRepository,
                          ProductRepository productRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    @Transactional
    public void run(String... args) {
        // Создаем пользователей, если их нет
        if (userRepository.count() == 0) {
            createUsers();
        }
        
        // Создаем категории, если их нет
        if (categoryRepository.count() == 0) {
            createCategories();
        }
        
        // Создаем поставщиков, если их нет
        if (supplierRepository.count() == 0) {
            createSuppliers();
        }
        
        // Создаем товары, если их нет
        if (productRepository.count() == 0) {
            createProducts();
        }
    }
    
    private void createUsers() {
        // Администратор
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setEmail("petrov.director@megamarket.ru");
        admin.setFirstName("Виктор");
        admin.setLastName("Петров");
        admin.setRole(Role.ADMIN);
        admin.setPhone("+7 (495) 123-45-67");
        userRepository.save(admin);
        
        // Менеджер
        User manager = new User();
        manager.setUsername("manager");
        manager.setPassword(passwordEncoder.encode("manager"));
        manager.setEmail("kozlov.logist@megamarket.ru");
        manager.setFirstName("Андрей");
        manager.setLastName("Козлов");
        manager.setRole(Role.MANAGER);
        manager.setPhone("+7 (495) 234-56-78");
        userRepository.save(manager);
        
        // Продавец
        User seller = new User();
        seller.setUsername("seller");
        seller.setPassword(passwordEncoder.encode("seller"));
        seller.setEmail("sidorova.cashier@megamarket.ru");
        seller.setFirstName("Мария");
        seller.setLastName("Сидорова");
        seller.setRole(Role.SELLER);
        seller.setPhone("+7 (495) 345-67-89");
        userRepository.save(seller);
        
        // Покупатель
        User customer = new User();
        customer.setUsername("customer");
        customer.setPassword(passwordEncoder.encode("customer"));
        customer.setEmail("fedorov.petr@mail.ru");
        customer.setFirstName("Петр");
        customer.setLastName("Федоров");
        customer.setRole(Role.CUSTOMER);
        customer.setPhone("+7 (495) 456-78-90");
        customer.setAddress("г. Москва, ул. Ленина, д. 42, кв. 89");
        userRepository.save(customer);
        
        System.out.println("✓ Пользователи созданы:");
        System.out.println("  - admin / admin (Администратор)");
        System.out.println("  - manager / manager (Менеджер)");
        System.out.println("  - seller / seller (Продавец)");
        System.out.println("  - customer / customer (Покупатель)");
    }
    
    private void createCategories() {
        categoryRepository.save(new Category("Молочные продукты", "Молоко, кефир, ряженка, йогурты, сыры, творог, сметана, сливки"));
        categoryRepository.save(new Category("Хлеб и выпечка", "Хлеб белый и черный, батоны, булочки, пирожные, торты"));
        categoryRepository.save(new Category("Мясо и птица", "Говядина, свинина, курица, индейка, субпродукты, фарш"));
        categoryRepository.save(new Category("Овощи и фрукты", "Свежие овощи, фрукты, ягоды, зелень, салатные смеси"));
        categoryRepository.save(new Category("Напитки", "Соки, вода, газированные напитки, чай, кофе, какао"));
        categoryRepository.save(new Category("Бакалея", "Крупы, макаронные изделия, мука, сахар, растительное масло"));
        categoryRepository.save(new Category("Кондитерские изделия", "Шоколад, конфеты, печенье, пряники, зефир, пастила"));
        categoryRepository.save(new Category("Рыба и морепродукты", "Рыба свежая и замороженная, креветки, кальмары, консервы"));
        categoryRepository.save(new Category("Замороженные продукты", "Замороженные овощи, ягоды, мясо, рыба, готовые блюда"));
        categoryRepository.save(new Category("Консервы", "Овощные, мясные, рыбные консервы, варенье, компоты"));
        
        System.out.println("✓ Категории созданы");
    }
    
    private void createSuppliers() {
        Supplier supplier1 = new Supplier();
        supplier1.setCompanyName("ООО \"Молочный Альянс\"");
        supplier1.setContactPerson("Николаева Ольга Валерьевна");
        supplier1.setPhone("+7 (495) 777-11-22");
        supplier1.setEmail("zakaz@molochny-alliance.ru");
        supplier1.setAddress("Московская область, г. Мытищи, ул. Заводская, д. 15");
        supplier1.setDescription("Ведущий производитель молочной продукции премиум-класса");
        supplierRepository.save(supplier1);
        
        Supplier supplier2 = new Supplier();
        supplier2.setCompanyName("ООО \"Хлебный дом\"");
        supplier2.setContactPerson("Лебедев Константин Дмитриевич");
        supplier2.setPhone("+7 (495) 888-22-33");
        supplier2.setEmail("sales@hlebdom.ru");
        supplier2.setAddress("г. Москва, ул. Пекарская, д. 8, стр. 2");
        supplier2.setDescription("Современная пекарня, производство свежего хлеба и кондитерских изделий");
        supplierRepository.save(supplier2);
        
        Supplier supplier3 = new Supplier();
        supplier3.setCompanyName("АО \"Мясные Традиции\"");
        supplier3.setContactPerson("Орлов Сергей Анатольевич");
        supplier3.setPhone("+7 (495) 999-33-44");
        supplier3.setEmail("info@meat-traditions.ru");
        supplier3.setAddress("Московская область, г. Балашиха, шоссе Энтузиастов, д. 25");
        supplier3.setDescription("Производство качественных мясных продуктов и деликатесов");
        supplierRepository.save(supplier3);
        
        Supplier supplier4 = new Supplier();
        supplier4.setCompanyName("ИП \"Овощная база\"");
        supplier4.setContactPerson("Романова Екатерина Игоревна");
        supplier4.setPhone("+7 (495) 111-44-55");
        supplier4.setEmail("zakaz@ovoschi-base.ru");
        supplier4.setAddress("г. Москва, ул. Торговая, д. 33, павильон 12");
        supplier4.setDescription("Оптовая торговля свежими овощами, фруктами и зеленью");
        supplierRepository.save(supplier4);
        
        Supplier supplier5 = new Supplier();
        supplier5.setCompanyName("ООО \"Рыбный промысел\"");
        supplier5.setContactPerson("Медведев Денис Владимирович");
        supplier5.setPhone("+7 (495) 222-55-66");
        supplier5.setEmail("order@fishprom.ru");
        supplier5.setAddress("г. Москва, ул. Рыбная, д. 17");
        supplier5.setDescription("Поставка свежей и замороженной рыбы, морепродуктов");
        supplierRepository.save(supplier5);
        
        System.out.println("✓ Поставщики созданы");
    }
    
    private void createProducts() {
        Category dairy = categoryRepository.findByName("Молочные продукты").orElse(null);
        Category bread = categoryRepository.findByName("Хлеб и выпечка").orElse(null);
        Category meat = categoryRepository.findByName("Мясо и птица").orElse(null);
        Category vegetables = categoryRepository.findByName("Овощи и фрукты").orElse(null);
        Category drinks = categoryRepository.findByName("Напитки").orElse(null);
        Category grocery = categoryRepository.findByName("Бакалея").orElse(null);
        Category sweets = categoryRepository.findByName("Кондитерские изделия").orElse(null);
        Category fish = categoryRepository.findByName("Рыба и морепродукты").orElse(null);
        Category frozen = categoryRepository.findByName("Замороженные продукты").orElse(null);
        Category canned = categoryRepository.findByName("Консервы").orElse(null);
        
        Supplier milkSupplier = supplierRepository.findByCompanyName("ООО \"Молочный Альянс\"").orElse(null);
        Supplier breadSupplier = supplierRepository.findByCompanyName("ООО \"Хлебный дом\"").orElse(null);
        Supplier meatSupplier = supplierRepository.findByCompanyName("АО \"Мясные Традиции\"").orElse(null);
        Supplier vegSupplier = supplierRepository.findByCompanyName("ИП \"Овощная база\"").orElse(null);
        Supplier fishSupplier = supplierRepository.findByCompanyName("ООО \"Рыбный промысел\"").orElse(null);
        
        // Молочные продукты
        createProduct("Молоко пастеризованное 2.5%", "Молоко пастеризованное, 900мл", new BigDecimal("89.90"), 150, "шт.", dairy, milkSupplier, 7);
        createProduct("Сыр Российский", "Сыр полутвердый классический, 300г", new BigDecimal("380.00"), 60, "шт.", dairy, milkSupplier, 60);
        createProduct("Творог 9%", "Творог мягкий классический, 250г", new BigDecimal("115.00"), 80, "шт.", dairy, milkSupplier, 7);
        createProduct("Йогурт клубничный", "Йогурт питьевой с кусочками клубники, 270мл", new BigDecimal("72.50"), 100, "шт.", dairy, milkSupplier, 14);
        createProduct("Сметана 20%", "Сметана классическая, 400г", new BigDecimal("128.00"), 90, "шт.", dairy, milkSupplier, 7);
        createProduct("Ряженка 3.2%", "Ряженка натуральная, 500мл", new BigDecimal("78.00"), 75, "шт.", dairy, milkSupplier, 7);
        createProduct("Масло сливочное 82.5%", "Масло сливочное несоленое, 200г", new BigDecimal("185.00"), 50, "шт.", dairy, milkSupplier, 30);
        
        // Хлеб и выпечка
        createProduct("Хлеб белый нарезной", "Хлеб пшеничный нарезной, 650г", new BigDecimal("62.00"), 80, "шт.", bread, breadSupplier, 3);
        createProduct("Батон нарезной", "Батон пшеничный нарезной, 500г", new BigDecimal("48.00"), 70, "шт.", bread, breadSupplier, 2);
        createProduct("Хлеб Бородинский", "Хлеб ржаной с тмином и кориандром, 600г", new BigDecimal("68.00"), 55, "шт.", bread, breadSupplier, 5);
        createProduct("Булочки сдобные", "Булочки сдобные с маком, 6 шт.", new BigDecimal("135.00"), 40, "уп.", bread, breadSupplier, 3);
        createProduct("Торт Наполеон", "Торт слоеный с заварным кремом, 800г", new BigDecimal("420.00"), 15, "шт.", bread, breadSupplier, 5);
        
        // Мясо и птица
        createProduct("Говядина мякоть", "Говядина для тушения, 1кг", new BigDecimal("750.00"), 20, "кг", meat, meatSupplier, 3);
        createProduct("Курица целая", "Курица охлажденная, 1.5кг", new BigDecimal("285.00"), 30, "шт.", meat, meatSupplier, 3);
        createProduct("Свинина шейка", "Свинина охлажденная, 1кг", new BigDecimal("590.00"), 18, "кг", meat, meatSupplier, 3);
        createProduct("Фарш говяжий", "Фарш говяжий свежий, 500г", new BigDecimal("380.00"), 25, "шт.", meat, meatSupplier, 1);
        createProduct("Колбаса Докторская", "Колбаса вареная высшего сорта, 300г", new BigDecimal("285.00"), 45, "шт.", meat, meatSupplier, 10);
        
        // Овощи и фрукты
        createProduct("Картофель", "Картофель столовый, 1кг", new BigDecimal("58.00"), 200, "кг", vegetables, vegSupplier, 30);
        createProduct("Помидоры", "Помидоры красные, 1кг", new BigDecimal("195.00"), 50, "кг", vegetables, vegSupplier, 7);
        createProduct("Огурцы", "Огурцы свежие, 1кг", new BigDecimal("175.00"), 45, "кг", vegetables, vegSupplier, 5);
        createProduct("Яблоки Гала", "Яблоки красные сладкие, 1кг", new BigDecimal("145.00"), 70, "кг", vegetables, vegSupplier, 30);
        createProduct("Бананы", "Бананы спелые, 1кг", new BigDecimal("108.00"), 80, "кг", vegetables, vegSupplier, 7);
        createProduct("Морковь", "Морковь столовая, 1кг", new BigDecimal("68.00"), 100, "кг", vegetables, vegSupplier, 21);
        createProduct("Апельсины", "Апельсины сладкие, 1кг", new BigDecimal("165.00"), 60, "кг", vegetables, vegSupplier, 14);
        createProduct("Лук репчатый", "Лук репчатый желтый, 1кг", new BigDecimal("48.00"), 120, "кг", vegetables, vegSupplier, 60);
        
        // Напитки
        createProduct("Сок апельсиновый", "Сок 100% прямого отжима, 1л", new BigDecimal("145.00"), 50, "шт.", drinks, null, 180);
        createProduct("Вода минеральная", "Вода минеральная газированная, 1л", new BigDecimal("52.00"), 100, "шт.", drinks, null, 730);
        createProduct("Лимонад", "Лимонад классический, 1.5л", new BigDecimal("125.00"), 70, "шт.", drinks, null, 180);
        createProduct("Кофе зерновой", "Кофе в зернах арабика, 500г", new BigDecimal("1250.00"), 20, "шт.", drinks, null, 730);
        createProduct("Чай черный листовой", "Чай черный байховый, 200г", new BigDecimal("285.00"), 40, "шт.", drinks, null, 730);
        
        // Бакалея
        createProduct("Рис круглозерный", "Рис для каши, 1кг", new BigDecimal("125.00"), 80, "шт.", grocery, null, 730);
        createProduct("Гречневая крупа", "Гречка ядрица, 800г", new BigDecimal("115.00"), 90, "шт.", grocery, null, 730);
        createProduct("Овсяные хлопья", "Овсянка быстрого приготовления, 400г", new BigDecimal("95.00"), 85, "шт.", grocery, null, 365);
        createProduct("Макароны спагетти", "Макароны из твердых сортов, 450г", new BigDecimal("88.00"), 95, "шт.", grocery, null, 730);
        createProduct("Сахар песок", "Сахар белый рафинированный, 1кг", new BigDecimal("95.00"), 100, "кг", grocery, null, 1825);
        createProduct("Масло подсолнечное", "Масло подсолнечное рафинированное, 1л", new BigDecimal("158.00"), 60, "шт.", grocery, null, 730);
        createProduct("Мука пшеничная", "Мука высшего сорта, 1кг", new BigDecimal("68.00"), 70, "кг", grocery, null, 365);
        
        // Кондитерские изделия
        createProduct("Шоколад молочный", "Шоколад молочный плитка, 100г", new BigDecimal("125.00"), 120, "шт.", sweets, null, 365);
        createProduct("Печенье овсяное", "Печенье овсяное с изюмом, 200г", new BigDecimal("95.00"), 80, "шт.", sweets, null, 180);
        createProduct("Конфеты шоколадные", "Конфеты ассорти шоколадные, 250г", new BigDecimal("285.00"), 50, "шт.", sweets, null, 180);
        createProduct("Вафли", "Вафли с начинкой ванильной, 200г", new BigDecimal("115.00"), 65, "шт.", sweets, null, 180);
        
        // Рыба и морепродукты
        createProduct("Лосось филе", "Лосось филе свежемороженый, 500г", new BigDecimal("850.00"), 15, "шт.", fish, fishSupplier, 180);
        createProduct("Креветки королевские", "Креветки замороженные очищенные, 400г", new BigDecimal("680.00"), 20, "шт.", fish, fishSupplier, 180);
        createProduct("Треска филе", "Треска филе замороженная, 600г", new BigDecimal("385.00"), 25, "шт.", fish, fishSupplier, 180);
        createProduct("Кальмары", "Кальмары тушка замороженные, 500г", new BigDecimal("420.00"), 18, "шт.", fish, fishSupplier, 180);
        
        // Замороженные продукты
        createProduct("Овощная смесь", "Смесь замороженных овощей, 450г", new BigDecimal("145.00"), 55, "шт.", frozen, null, 730);
        createProduct("Пельмени классические", "Пельмени говяжьи, 500г", new BigDecimal("285.00"), 40, "шт.", frozen, null, 180);
        createProduct("Мороженое пломбир", "Мороженое пломбир в стаканчике, 80г", new BigDecimal("85.00"), 100, "шт.", frozen, null, 90);
        
        // Консервы
        createProduct("Тушенка говяжья", "Говядина тушеная, 325г", new BigDecimal("285.00"), 35, "шт.", canned, null, 1095);
        createProduct("Консервы рыбные", "Сайра в масле, 240г", new BigDecimal("165.00"), 50, "шт.", canned, null, 1095);
        createProduct("Горошек зеленый", "Горошек зеленый консервированный, 425г", new BigDecimal("95.00"), 60, "шт.", canned, null, 730);
        
        System.out.println("✓ Товары созданы");
    }
    
    private void createProduct(String name, String description, BigDecimal price, int quantity,
                               String unit, Category category, Supplier supplier, int shelfLifeDays) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setUnit(unit);
        product.setCategory(category);
        product.setSupplier(supplier);
        product.setManufactureDate(LocalDate.now());
        product.setExpirationDate(LocalDate.now().plusDays(shelfLifeDays));
        product.setAvailable(true);
        productRepository.save(product);
    }
}


