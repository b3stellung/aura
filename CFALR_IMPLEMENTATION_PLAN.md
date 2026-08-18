# CFALR 个性化穿搭推荐系统实施计划

## 总体架构

```
┌─────────────────────────────────────────────────────────┐
│                    用户查询                              │
│         "约会穿什么好看？"                               │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                 查询理解 & 路由                          │
│         解析意图、场景、用户画像                         │
└─────────────────────────────────────────────────────────┘
                          │
              ┌───────────┴───────────┐
              ▼                       ▼
┌─────────────────────┐   ┌─────────────────────┐
│    CF 塔             │   │    语义塔            │
│  用户行为嵌入        │   │  LLM 语义嵌入        │
│  (协同过滤信号)      │   │  (美学理解)          │
└─────────────────────┘   └─────────────────────┘
              │                       │
              └───────────┬───────────┘
                          ▼
┌─────────────────────────────────────────────────────────┐
│                    融合层                                │
│           交叉注意力 / MLP 融合                         │
│           个性化 + 美学 = 最终推荐                      │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                    输出层                                │
│           Top-K 穿搭推荐 + 个性化解释                   │
└─────────────────────────────────────────────────────────┘
```

---

## Phase 1：用户行为数据模型（Day 1）

### 1.1 数据实体设计

```java
// 用户行为记录
@Entity
@Table(name = "user_behavior")
public class UserBehavior {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private UUID userId;
    private UUID itemId;           // 衣物ID
    private BehaviorType type;     // VIEW, FAVORITE, WEAR, RATE
    private Integer rating;        // 评分1-5（仅RATE类型）
    private String occasion;       // 场合（仅WEAR类型）
    private LocalDateTime createdAt;
}

// 用户穿搭记录
@Entity
@Table(name = "outfit_records")
public class OutfitRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private UUID userId;
    private String occasion;       // 约会、职场、休闲
    private String season;         // 春夏秋冬
    private String mood;           // 风格心情
    private Integer satisfaction;  // 满意度1-5
    
    @ElementCollection
    private List<UUID> itemIds;    // 穿搭单品列表
    
    private LocalDateTime createdAt;
}
```

### 1.2 行为采集接口

```java
@RestController
@RequestMapping("/api/v1/behavior")
public class BehaviorController {
    
    @PostMapping("/view")      // 浏览
    @PostMapping("/favorite")  // 收藏
    @PostMapping("/wear")      // 穿搭记录
    @PostMapping("/rate")      // 评分
}
```

---

## Phase 2：CF 嵌入模型（Day 2-3）

### 2.1 矩阵分解方案（轻量级）

```java
@Service
public class CFEmbeddingService {
    
    // 用户嵌入矩阵: userId -> float[64]
    // 物品嵌入矩阵: itemId -> float[64]
    
    // 训练：ALS (Alternating Least Squares)
    // 输入：用户-物品交互矩阵
    // 输出：用户向量 + 物品向量
}
```

### 2.2 神经协同过滤方案（标准版）

```java
@Service
public class NeuralCFService {
    
    // 双塔网络：
    // 用户塔：[用户ID嵌入, 行为序列嵌入] -> MLP -> 用户向量
    // 物品塔：[物品ID嵌入, 属性嵌入] -> MLP -> 物品向量
    
    // 训练目标：BPR (Bayesian Personalized Ranking)
}
```

### 2.3 实现选择

**推荐：矩阵分解（ALS）**
- 实现简单，Java生态成熟（Spark MLlib / Mahout）
- 训练快，推理快
- 适合冷启动（可通过用户画像初始化）

---

## Phase 3：语义嵌入增强（Day 3）

### 3.1 穿搭描述向量化

```java
@Service
public class SemanticEmbeddingService {
    
    // 衣物描述 -> text-embedding-v4 -> float[1024]
    // 美学知识 -> text-embedding-v4 -> float[1024]
    // 用户偏好描述 -> text-embedding-v4 -> float[1024]
}
```

### 3.2 多模态扩展（可选）

```java
// 图像嵌入（后续）
// 衣物图片 -> FashionSigLIP -> float[768]
// 统一到语义空间
```

---

## Phase 4：融合机制（Day 4-5）

### 4.1 双塔架构

```java
@Service
public class FusionService {
    
    /**
     * 融合 CF 嵌入和语义嵌入
     * 
     * @param cfVector    CF嵌入 (64维)
     * @param semanticVector  语义嵌入 (1024维)
     * @return 融合向量 (256维)
     */
    public float[] fuse(float[] cfVector, float[] semanticVector) {
        // 1. 维度对齐：CF 64 -> 256, 语义 1024 -> 256
        // 2. 交叉注意力或MLP融合
        // 3. 输出融合向量
    }
}
```

### 4.2 融合策略

**方案A：加权拼接（简单）**
```java
float[] fused = concat(cfVector * alpha, semanticVector * (1-alpha));
```

**方案B：MLP融合（推荐）**
```java
// 输入：[cf_256; semantic_256] (512维)
// MLP: 512 -> 256 -> 128
// 输出：融合向量 128维
```

**方案C：交叉注意力（高级）**
```java
// Query: semanticVector
// Key/Value: cfVector
// 输出：注意力加权融合
```

---

## Phase 5：个性化推荐（Day 5）

### 5.1 推荐流程

```java
@Service
public class PersonalizedRecommendationService {
    
    public List<OutfitRecommendation> recommend(String userId, String query) {
        // 1. 获取用户CF嵌入
        float[] userCF = cfService.getUserEmbedding(userId);
        
        // 2. 查询语义嵌入
        float[] querySemantic = semanticService.embed(query);
        
        // 3. 融合
        float[] fused = fusionService.fuse(userCF, querySemantic);
        
        // 4. 向量检索（融合向量 vs 候选物品向量）
        List<Item> candidates = vectorStore.search(fused, topK);
        
        // 5. 重排序（考虑多样性、新颖性）
        return rerank(candidates, userPreferences);
    }
}
```

---

## 实施步骤

### Day 1：数据模型 + 采集接口
- [x] 创建 UserBehavior 实体
- [x] 创建 OutfitRecord 实体
- [x] 实现 BehaviorController
- [ ] 数据库迁移

### Day 2：CF嵌入训练
- [x] 实现 ALS 矩阵分解
- [ ] 定时训练任务
- [ ] 用户/物品向量存储

### Day 3：语义嵌入增强
- [x] 穿搭描述向量化
- [x] 美学知识向量化
- [x] 用户偏好向量化

### Day 4：融合机制
- [x] 实现 MLP 融合层
- [x] 维度对齐
- [x] 融合向量生成

### Day 5：个性化推荐
- [x] 推荐服务集成
- [ ] 端到端测试
- [ ] 效果评估

---

## 技术栈

| 组件 | 方案 | 备选 |
|------|------|------|
| CF训练 | 自实现ALS | Spark MLlib |
| 语义嵌入 | DashScope text-embedding-v4 | BGE-M3 |
| 向量存储 | Milvus | Qdrant |
| 融合模型 | Java MLP | ONNX Runtime |
| 推荐框架 | 自研 | Spring AI Advisor |

---

## 验收标准

1. **冷启动**：新用户基于语义推荐
2. **个性化**：老用户推荐融入行为偏好
3. **多样性**：推荐结果不重复
4. **响应时间**：< 2s
5. **准确率**：Top-5 命中率 > 60%
