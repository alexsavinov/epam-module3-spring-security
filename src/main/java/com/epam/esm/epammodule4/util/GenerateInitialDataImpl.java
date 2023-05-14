package com.epam.esm.epammodule4.util;

import com.epam.esm.epammodule4.model.entity.GiftCertificate;
import com.epam.esm.epammodule4.model.entity.Order;
import com.epam.esm.epammodule4.model.entity.Tag;
import com.epam.esm.epammodule4.model.entity.User;
import com.epam.esm.epammodule4.repository.GiftCertificateRepository;
import com.epam.esm.epammodule4.repository.OrderRepository;
import com.epam.esm.epammodule4.repository.TagRepository;
import com.epam.esm.epammodule4.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.randname.RandomNameGenerator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Optional.ofNullable;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateInitialDataImpl {

    private final TagRepository tagRepository;
    private final GiftCertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public void generate(int numberOfRecords) {
        log.info("Generating entities for {} records each", numberOfRecords);
        RandomNameGenerator rnd = new RandomNameGenerator();

        Tag previousTag = null;

        for (int i = 0; i < numberOfRecords; i++) {
            Tag tag = generateTag(rnd);
            tagRepository.save(tag);

            GiftCertificate cert = generateGiftCertificate(rnd, previousTag, tag);
            previousTag = tag;
            certificateRepository.save(cert);

            User user = generateUser(rnd);
            userRepository.save(user);

            Order order = generateOrder(cert, user);
            orderRepository.save(order);
        }

        log.info("Generated entities finished");
    }

    private static Order generateOrder(GiftCertificate cert, User user) {
        Order order = Order.builder()
                .user(user)
                .giftCertificate(cert)
                .price(cert.getPrice())
                .build();
        return order;
    }

    private User generateUser(RandomNameGenerator rnd) {
        String name = rnd.next();
        User user = User.builder()
                .name(addPrefix(name, User.class.getSimpleName()))
                .username(name)
                .password(name)
                .email(name + "@mail.com")
                .build();
        return user;
    }

    private GiftCertificate generateGiftCertificate(RandomNameGenerator rnd, Tag previousTag, Tag tag) {
        double price = getRandomInt(1, 100) * 1.55;

        GiftCertificate cert = GiftCertificate.builder()
                .name(addPrefix(rnd.next(), GiftCertificate.class.getSimpleName()))
                .description(capitalizeWord(rnd.next()))
                .duration(getRandomInt(1, 10))
                .price((double) Math.round(price * 100) / 100)
                .build();

        List<Tag> tags = new ArrayList<>();

        tags.add(tag);
        ofNullable(previousTag).ifPresent(tags::add);

        cert.setTags(tags);
        return cert;
    }

    private Tag generateTag(RandomNameGenerator rnd) {
        Tag tag = Tag.builder()
                .name(addPrefix(rnd.next(), Tag.class.getSimpleName()))
                .build();
        return tag;
    }

    private String addPrefix(String word, String prefix) {
        return "[%s] %s".formatted(prefix, capitalizeWord(word));
    }

    private String capitalizeWord(String word) {
        return StringUtils.capitalize(word.replace("_", " "));
    }

    private int getRandomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }
}

/*
INSERT INTO role(name) VALUES('ROLE_USER');
INSERT INTO role(name) VALUES('ROLE_ADMIN');
*/