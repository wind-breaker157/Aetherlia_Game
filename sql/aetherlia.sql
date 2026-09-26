-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Sep 26, 2026 at 11:41 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `aetherlia`
--

-- --------------------------------------------------------

--
-- Table structure for table `inventory_items`
--

CREATE TABLE `inventory_items` (
  `id` bigint(20) NOT NULL,
  `item_code` varchar(50) NOT NULL,
  `quantity` int(11) NOT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `inventory_items`
--

INSERT INTO `inventory_items` (`id`, `item_code`, `quantity`, `user_id`) VALUES
(1, 'BASIC_ORB', 54, 1),
(2, 'GREAT_ORB', 2, 1),
(3, 'ULTRA_ORB', 2, 1),
(4, 'MASTER_ORB', 1, 1);

-- --------------------------------------------------------

--
-- Table structure for table `map_areas`
--

CREATE TABLE `map_areas` (
  `id` bigint(20) NOT NULL,
  `code` varchar(255) NOT NULL,
  `leader_boss` varchar(255) DEFAULT NULL,
  `map_number` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `terrain` varchar(255) NOT NULL,
  `unlock_condition` varchar(500) NOT NULL,
  `unlocked_by_default` bit(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `map_areas`
--

INSERT INTO `map_areas` (`id`, `code`, `leader_boss`, `map_number`, `name`, `terrain`, `unlock_condition`, `unlocked_by_default`) VALUES
(1, 'luc_diep', 'Tutorial, đấu Rival', 1, 'Làng Lục Diệp', 'Đồng cỏ, làng', 'Mở sẵn', b'1'),
(2, 'thinh_wisper', 'L1 Water: Marina Tidewell', 2, 'Rừng Thì Thầm', 'Rừng rậm, sương mù', 'Hoàn thành Main Quest 1 + đánh Rival lần 1', b'0'),
(3, 'hang_pha_le', 'L2 Fire: Ignar Cinderfell', 3, 'Hang Pha Lê', 'Hang động, tinh thể', 'Có Huy hiệu 1 + kỹ năng Phá Đá', b'0'),
(4, 'thung_lung_bao', 'L3 Electric: Volta Sparkmoor', 4, 'Thung Lũng Bão', 'Đồi gió, cột sét', 'Hoàn thành cốt truyện Trạm Đào Bị Chiếm', b'0'),
(5, 'vuong_quoc_sa_mac', 'L4 Nature: Sylva Thornreed', 5, 'Vương Quốc Sa Mạc', 'Sa mạc, ốc đảo', 'Có 3 Huy hiệu', b'0'),
(6, 'nui_bang_gia', 'L5 Ice: Frost Elowen', 6, 'Núi Băng Giá', 'Tuyết, sông băng', 'Kỹ năng Vượt Băng + 4 Huy hiệu', b'0'),
(7, 'di_tich_co', 'L6 Dark: Nyx Morrowind; L7 Psychic: Orin Veilsight', 7, 'Di Tích Cổ', 'Tàn tích, mê cung', 'Có 6 Huy hiệu', b'0'),
(8, 'thu_do_aurelia', 'L8 Dragon: Zephra Drakemoor', 8, 'Thủ Đô Aurelia', 'Đô thị, sân vận động', 'Hoàn thành Sự Thật Di Tích', b'0'),
(9, 'dao_vo_dich', 'Champion + Selvane', 9, 'Đảo Vô Địch', 'Đảo nổi', 'Đủ 8 Huy hiệu', b'0');

-- --------------------------------------------------------

--
-- Table structure for table `monsters`
--

CREATE TABLE `monsters` (
  `id` bigint(20) NOT NULL,
  `base_attack` int(11) NOT NULL,
  `base_defense` int(11) NOT NULL,
  `base_hp` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `base_exp` int(11) NOT NULL,
  `catch_rate` double NOT NULL,
  `rarity` varchar(255) NOT NULL,
  `base_speed` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `monsters`
--

INSERT INTO `monsters` (`id`, `base_attack`, `base_defense`, `base_hp`, `name`, `type`, `base_exp`, `catch_rate`, `rarity`, `base_speed`) VALUES
(1, 49, 49, 45, 'Spriglet', 'GRASS', 50, 1, 'Common', 65),
(2, 52, 43, 39, 'Pyron', 'FIRE', 52, 1, 'Common', 60),
(3, 48, 65, 44, 'Aquaff', 'WATER', 55, 1, 'Common', 50);

-- --------------------------------------------------------

--
-- Table structure for table `player_characters`
--

CREATE TABLE `player_characters` (
  `id` bigint(20) NOT NULL,
  `facing` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `sprite_code` varchar(255) NOT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `player_characters`
--

INSERT INTO `player_characters` (`id`, `facing`, `name`, `sprite_code`, `user_id`) VALUES
(1, 'LEFT', 'admin', 'player_default', 1);

-- --------------------------------------------------------

--
-- Table structure for table `player_maps`
--

CREATE TABLE `player_maps` (
  `id` bigint(20) NOT NULL,
  `unlocked` bit(1) NOT NULL,
  `map_area_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `player_maps`
--

INSERT INTO `player_maps` (`id`, `unlocked`, `map_area_id`, `user_id`) VALUES
(1, b'1', 1, 1),
(2, b'0', 2, 1),
(3, b'0', 3, 1),
(4, b'0', 4, 1),
(5, b'0', 5, 1),
(6, b'0', 6, 1),
(7, b'0', 7, 1),
(8, b'0', 8, 1),
(9, b'0', 9, 1);

-- --------------------------------------------------------

--
-- Table structure for table `player_monsters`
--

CREATE TABLE `player_monsters` (
  `id` bigint(20) NOT NULL,
  `current_hp` int(11) NOT NULL,
  `exp` int(11) NOT NULL,
  `level` int(11) NOT NULL,
  `monster_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `player_monsters`
--

INSERT INTO `player_monsters` (`id`, `current_hp`, `exp`, `level`, `monster_id`, `user_id`) VALUES
(1, 53, 487, 9, 1, 1),
(2, 49, 0, 4, 1, 1),
(3, 0, 0, 4, 3, 1),
(4, 40, 0, 2, 2, 1),
(5, 25, 36, 5, 2, 1),
(6, 17, 0, 3, 3, 1),
(7, 0, 0, 5, 1, 1),
(8, 40, 61, 4, 1, 1),
(9, 45, 0, 2, 3, 1),
(10, 48, 0, 3, 1, 1);

-- --------------------------------------------------------

--
-- Table structure for table `player_positions`
--

CREATE TABLE `player_positions` (
  `id` bigint(20) NOT NULL,
  `location_code` varchar(255) NOT NULL,
  `map_area_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `player_positions`
--

INSERT INTO `player_positions` (`id`, `location_code`, `map_area_id`, `user_id`, `x`, `y`) VALUES
(1, 'route_grass', 1, 1, 304, 452);

-- --------------------------------------------------------

--
-- Table structure for table `teams`
--

CREATE TABLE `teams` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `teams`
--

INSERT INTO `teams` (`id`, `user_id`) VALUES
(1, 1);

-- --------------------------------------------------------

--
-- Table structure for table `team_members`
--

CREATE TABLE `team_members` (
  `id` bigint(20) NOT NULL,
  `position` int(11) NOT NULL,
  `player_monster_id` bigint(20) NOT NULL,
  `team_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `team_members`
--

INSERT INTO `team_members` (`id`, `position`, `player_monster_id`, `team_id`) VALUES
(4, 1, 1, 1),
(6, 2, 3, 1),
(8, 3, 5, 1),
(9, 4, 6, 1),
(10, 5, 7, 1),
(11, 6, 8, 1);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `email`, `password`, `username`) VALUES
(1, 'admin@gmail.com', '$2a$10$EUgoENIB.cuRqZNQA8McrOO36YdXGbQ7DIzyCuz7PWwiL93o4aTlC', 'admin');

-- --------------------------------------------------------

--
-- Table structure for table `wild_encounters`
--

CREATE TABLE `wild_encounters` (
  `id` bigint(20) NOT NULL,
  `active` bit(1) NOT NULL,
  `current_hp` int(11) NOT NULL,
  `level` int(11) NOT NULL,
  `monster_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `wild_encounters`
--

INSERT INTO `wild_encounters` (`id`, `active`, `current_hp`, `level`, `monster_id`, `user_id`) VALUES
(1, b'0', 0, 2, 1, 1),
(2, b'0', 44, 3, 1, 1),
(3, b'0', 42, 2, 1, 1),
(4, b'0', 0, 2, 3, 1),
(5, b'0', 0, 3, 3, 1),
(6, b'0', 32, 3, 2, 1),
(7, b'0', 0, 2, 1, 1),
(8, b'0', 49, 5, 1, 1),
(9, b'0', 0, 3, 3, 1),
(10, b'0', 0, 2, 1, 1),
(11, b'0', 25, 3, 3, 1),
(12, b'0', 25, 5, 2, 1),
(13, b'0', 9, 2, 2, 1),
(14, b'0', 0, 4, 1, 1),
(15, b'0', 40, 5, 1, 1),
(16, b'0', 6, 2, 2, 1),
(17, b'0', 21, 5, 2, 1),
(18, b'0', 0, 5, 3, 1),
(19, b'0', 0, 3, 3, 1),
(20, b'0', 27, 2, 2, 1),
(21, b'0', 1, 2, 2, 1),
(22, b'0', 0, 3, 2, 1),
(23, b'0', 25, 2, 3, 1),
(24, b'0', 0, 4, 3, 1),
(25, b'0', 14, 5, 1, 1),
(26, b'0', 0, 3, 1, 1),
(27, b'0', 19, 5, 1, 1),
(28, b'0', 0, 4, 1, 1),
(29, b'0', 0, 3, 1, 1),
(30, b'0', 31, 4, 2, 1),
(31, b'0', 0, 4, 3, 1),
(32, b'0', 0, 2, 1, 1),
(33, b'0', 21, 5, 2, 1),
(34, b'0', 0, 5, 3, 1),
(35, b'0', 0, 3, 3, 1),
(36, b'0', 0, 5, 3, 1),
(37, b'0', 0, 2, 2, 1),
(38, b'0', 0, 5, 2, 1),
(39, b'0', 0, 4, 1, 1),
(40, b'0', 0, 2, 1, 1),
(41, b'0', 0, 4, 3, 1),
(42, b'0', 35, 3, 1, 1),
(43, b'0', 0, 3, 3, 1),
(44, b'0', 0, 5, 1, 1),
(45, b'0', 0, 4, 1, 1),
(46, b'0', 0, 2, 3, 1),
(47, b'0', 21, 2, 2, 1),
(48, b'0', 12, 5, 2, 1),
(49, b'0', 0, 2, 1, 1),
(50, b'0', 0, 2, 1, 1),
(51, b'0', 0, 3, 3, 1),
(52, b'0', 33, 3, 3, 1),
(53, b'0', 24, 2, 2, 1),
(54, b'0', 0, 3, 1, 1),
(55, b'0', 34, 2, 1, 1),
(56, b'0', 0, 5, 3, 1),
(57, b'0', 6, 4, 2, 1),
(58, b'1', 15, 2, 3, 1);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `inventory_items`
--
ALTER TABLE `inventory_items`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKo8k5lgxxoah97q7bmf9yfgmq1` (`user_id`,`item_code`);

--
-- Indexes for table `map_areas`
--
ALTER TABLE `map_areas`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK9wpck563yirnyxc9398g4j5dn` (`code`),
  ADD UNIQUE KEY `UKa98ia8t64gf9oye2pew8sdre2` (`map_number`);

--
-- Indexes for table `monsters`
--
ALTER TABLE `monsters`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `player_characters`
--
ALTER TABLE `player_characters`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK9eu7hvvsrm94kf4uljrbxw4tw` (`user_id`);

--
-- Indexes for table `player_maps`
--
ALTER TABLE `player_maps`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKhq382p4j8t9jbobjdlk3xnk4m` (`user_id`,`map_area_id`),
  ADD KEY `FKeah88gb4yuyvdipi6pk8qke5n` (`map_area_id`);

--
-- Indexes for table `player_monsters`
--
ALTER TABLE `player_monsters`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKsyag7iw6moh1yt41lpkcmi2ji` (`monster_id`),
  ADD KEY `FKl8bb24mfo9n27y9iugnrxtopg` (`user_id`);

--
-- Indexes for table `player_positions`
--
ALTER TABLE `player_positions`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK3w4iq6ix9uth76waoc7ibv4l` (`user_id`),
  ADD KEY `FKcsqyt4unbmsv4heg6ob4hgduq` (`map_area_id`);

--
-- Indexes for table `teams`
--
ALTER TABLE `teams`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKgo1rp6g0b6kqfrkwj5pyt00w5` (`user_id`);

--
-- Indexes for table `team_members`
--
ALTER TABLE `team_members`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK7ui7q5406ia19akjdnua8wsbh` (`player_monster_id`),
  ADD KEY `FKtgca08el3ofisywcf11f0f76t` (`team_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  ADD UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`);

--
-- Indexes for table `wild_encounters`
--
ALTER TABLE `wild_encounters`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK7ly19egjtkunvpkj8ue2dth95` (`monster_id`),
  ADD KEY `FKge9atv56ayl4alfxj2bg2f0xa` (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `inventory_items`
--
ALTER TABLE `inventory_items`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `map_areas`
--
ALTER TABLE `map_areas`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `monsters`
--
ALTER TABLE `monsters`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `player_characters`
--
ALTER TABLE `player_characters`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `player_maps`
--
ALTER TABLE `player_maps`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `player_monsters`
--
ALTER TABLE `player_monsters`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `player_positions`
--
ALTER TABLE `player_positions`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `teams`
--
ALTER TABLE `teams`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `team_members`
--
ALTER TABLE `team_members`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `wild_encounters`
--
ALTER TABLE `wild_encounters`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=59;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `inventory_items`
--
ALTER TABLE `inventory_items`
  ADD CONSTRAINT `FK7bw09xyg2ucgdjyif5oxd9btg` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `player_characters`
--
ALTER TABLE `player_characters`
  ADD CONSTRAINT `FKq01p4omqbgb28pby2t9nfqhno` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `player_maps`
--
ALTER TABLE `player_maps`
  ADD CONSTRAINT `FKeah88gb4yuyvdipi6pk8qke5n` FOREIGN KEY (`map_area_id`) REFERENCES `map_areas` (`id`),
  ADD CONSTRAINT `FKhgm4x2lmfg495030o90wcdwun` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `player_monsters`
--
ALTER TABLE `player_monsters`
  ADD CONSTRAINT `FKl8bb24mfo9n27y9iugnrxtopg` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `FKsyag7iw6moh1yt41lpkcmi2ji` FOREIGN KEY (`monster_id`) REFERENCES `monsters` (`id`);

--
-- Constraints for table `player_positions`
--
ALTER TABLE `player_positions`
  ADD CONSTRAINT `FKcsqyt4unbmsv4heg6ob4hgduq` FOREIGN KEY (`map_area_id`) REFERENCES `map_areas` (`id`),
  ADD CONSTRAINT `FKob7hg32cvuhdq7p0r468j755b` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `teams`
--
ALTER TABLE `teams`
  ADD CONSTRAINT `FKlm88j38y90erf2rum00e85gw8` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `team_members`
--
ALTER TABLE `team_members`
  ADD CONSTRAINT `FK7ui7q5406ia19akjdnua8wsbh` FOREIGN KEY (`player_monster_id`) REFERENCES `player_monsters` (`id`),
  ADD CONSTRAINT `FKtgca08el3ofisywcf11f0f76t` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`);

--
-- Constraints for table `wild_encounters`
--
ALTER TABLE `wild_encounters`
  ADD CONSTRAINT `FK7ly19egjtkunvpkj8ue2dth95` FOREIGN KEY (`monster_id`) REFERENCES `monsters` (`id`),
  ADD CONSTRAINT `FKge9atv56ayl4alfxj2bg2f0xa` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
