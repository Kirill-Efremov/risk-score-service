package ru.kpfu.itis.efremov.schemarisk.core.recommend;

import org.springframework.stereotype.Service;
import ru.kpfu.itis.efremov.schemarisk.core.diff.FieldChange;
import ru.kpfu.itis.efremov.schemarisk.core.diff.DiffResult;
import ru.kpfu.itis.efremov.schemarisk.core.engine.CompatibilityResult;
import ru.kpfu.itis.efremov.schemarisk.core.risk.RiskResult;
import ru.kpfu.itis.efremov.schemarisk.model.Issue;
import ru.kpfu.itis.efremov.schemarisk.model.IssueSeverity;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationService {


    public List<String> generateRecommendations(CompatibilityResult compatibilityResult,
                                                DiffResult diffResult,
                                                RiskResult riskResult) {
        List<String> recs = new ArrayList<>();

        switch (riskResult.getRiskLevel()) {
            case HIGH -> recs.add(
                    "Высокий риск. Не рекомендуется деплоить изменения без плана миграции и координации с потребителями."
            );
            case MEDIUM -> recs.add(
                    "Средний риск. Перед деплоем оцените влияние на ключевые консьюмеры и проведите тестирование."
            );
            case LOW -> recs.add(
                    "Низкий риск. Тем не менее, рекомендуется уведомить команды-потребители и обновить документацию."
            );
        }

        for (Issue issue : compatibilityResult.getIssues()) {
            if (issue.getSeverity() == IssueSeverity.ERROR) {
                recs.add(
                        "Исправьте несовместимость Avro: "
                                + issue.getCode() + " — " + issue.getMessage()
                );
            }
        }

        if (diffResult != null && diffResult.getChanges() != null) {
            for (FieldChange ch : diffResult.getChanges()) {
                switch (ch.getType()) {
                    case REMOVED -> recs.add(
                            "Поле '" + ch.getFieldName()
                                    + "' удалено. Рассмотрите вариант оставить поле и пометить как deprecated, "
                                    + "либо выполнить поэтапную миграцию всех потребителей."
                    );
                    case TYPE_CHANGED -> recs.add(
                            "Тип поля '" + ch.getFieldName()
                                    + "' изменён с " + ch.getOldType() + " на " + ch.getNewType()
                                    + ". Рекомендуется добавить новое поле с новым типом, а старое оставить для совместимости."
                    );
                    case ADDED -> {
                        if (ch.getNewDefault() == null) {
                            recs.add(
                                    "Добавлено поле '" + ch.getFieldName()
                                            + "' без default. Для backward-совместимости добавьте default "
                                            + "(часто используют union [\"null\", <type>])."
                            );
                        } else {
                            recs.add(
                                    "Добавлено поле '" + ch.getFieldName()
                                            + "' с default. Проверьте, что значение по умолчанию корректно в бизнес-контексте."
                            );
                        }
                    }
                    case DEFAULT_ADDED -> recs.add(
                            "Для поля '" + ch.getFieldName()
                                    + "' добавлен default. Это улучшает совместимость со старыми сообщениями."
                    );
                    case DEFAULT_REMOVED -> recs.add(
                            "Удалён default для поля '" + ch.getFieldName()
                                    + "'. Поле может стать фактически обязательным и сломать старые данные."
                    );
                    case OTHER -> {
                    }
                }
            }
        }

        return recs;
    }
}
