package com.mindary.diary.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "diaries", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id"})})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = "images")
public class DiaryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private UUID id;

    @NotBlank(message = "Diary content should not be empty")
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @NotBlank(message = "Diary encrypt key should not be empty")
    @Column(name = "aes_key", columnDefinition = "TEXT")
    private String aesKey;

    @NotBlank(message = "IV should not be empty")
    @Column(name = "iv", columnDefinition = "TEXT")
    private String aesIv;

    @Column(name = "user_id")
    private UUID userId;

    @OneToMany(
            mappedBy = "diary",
            cascade = {CascadeType.ALL},
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<DiaryImage> images;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    public void addImage(DiaryImage image) {
        if (images == null) {
            images = new HashSet<>();
        }
        images.add(image);
    }
}
