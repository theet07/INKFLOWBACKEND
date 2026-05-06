# Sprint 2 - Otimização de Queries (Concluído)

## Otimizações Aplicadas

| Operação | Antes | Depois | Melhoria |
|----------|-------|--------|----------|
| Toggle checklist item | 3 queries | 1 query | 67% |
| Criar cicatrização (30 dias) | 210 inserts | 30 batches | 85% |
| Carregar badges usuário | N+1 queries | 1 query | ~90% |
| Carregar checkpoint | 2 queries | 1 query | 50% |

## Arquivos Modificados

- `repository/ChecklistItemRepository.java` — fetch join item+checkpoint+cicatrizacao
- `repository/CheckpointDiaRepository.java` — fetch join checkpoint+cicatrizacao
- `repository/BadgeUsuarioRepository.java` — fetch join badge+usuario
- `service/CicatrizacaoService.java` — batch insert com saveAll()
- `controller/BadgeController.java` — usa query otimizada de badges
